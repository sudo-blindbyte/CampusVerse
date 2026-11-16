import { Request, Response } from 'express';
import { z } from 'zod';
import { prisma } from '../services/prisma.service';
import { hashPassword, comparePassword } from '../utils/password';
import { signToken } from '../utils/jwt';
import { sendSuccess, sendError } from '../utils/response';
import { otpService } from '../services/otp.service';

export const registerSchema = z.object({
  name: z.string().optional(),
  fullName: z.string().optional(),
  email: z.string().email('Valid email address is required').max(150),
  password: z.string().min(1, 'Password is required'),
  role: z.string().optional().default('STUDENT')
});

export const loginSchema = z.object({
  email: z.string().email('Valid email address is required'),
  password: z.string().min(1, 'Password is required')
});

export const sendOtpSchema = z.object({
  email: z.string().email('Valid email address is required'),
  purpose: z
    .enum(['EMAIL_VERIFICATION', 'PASSWORD_RESET', 'ACCOUNT_RECOVERY'])
    .default('EMAIL_VERIFICATION')
});

export const verifyOtpSchema = z.object({
  email: z.string().email('Valid email address is required'),
  otp: z.string().length(6, 'Verification code must be 6 digits'),
  purpose: z
    .enum(['EMAIL_VERIFICATION', 'PASSWORD_RESET', 'ACCOUNT_RECOVERY'])
    .default('EMAIL_VERIFICATION')
});

export const forgotPasswordSchema = z.object({
  email: z.string().email('Valid email address is required')
});

export const resetPasswordSchema = z.object({
  email: z.string().email('Valid email address is required'),
  otp: z.string().min(6, 'Verification code must be at least 6 characters').max(64, 'Verification code must not exceed 64 characters'),
  newPassword: z.string().min(1, 'Password is required')
});

export async function register(req: Request, res: Response): Promise<void> {
  const { name, fullName, email, password, role } = req.body;
  const userName = (name || fullName || email.split('@')[0] || 'User').trim();
  const normalizedEmail = email.trim().toLowerCase();
  const roleUpper = (role || 'STUDENT').toUpperCase();

  const existingUser = await prisma.user.findUnique({
    where: { email: normalizedEmail }
  });

  if (existingUser) {
    sendError(res, 'An account with this email address already exists.', 409, 'EMAIL_EXISTS');
    return;
  }

  const passwordHash = await hashPassword(password);

  const isAdminAuthorized = (roleUpper === 'ADMIN');

  const newUser = await prisma.user.create({
    data: {
      email: normalizedEmail,
      passwordHash,
      role: roleUpper,
      isEmailVerified: true,
      isAdminAuthorized,
      isActive: true,
      profile: {
        create: {
          fullName: name.trim()
        }
      },
      privacySettings: { create: {} },
      securitySettings: { create: {} }
    },
    select: {
      id: true,
      email: true,
      role: true,
      isEmailVerified: true,
      isAdminAuthorized: true,
      createdAt: true,
      profile: {
        select: {
          id: true,
          fullName: true,
          bio: true,
          avatarUrl: true
        }
      }
    }
  });

  // Automatically dispatch secure verification OTP email
  await otpService.sendOtp(normalizedEmail, 'EMAIL_VERIFICATION');

  const token = signToken({
    userId: newUser.id,
    email: newUser.email,
    role: newUser.role,
    isAdminAuthorized: newUser.isAdminAuthorized
  });

  sendSuccess(
    res,
    {
      token,
      user: {
        userId: newUser.id,
        email: newUser.email,
        name: newUser.profile?.fullName || name,
        role: newUser.role,
        isEmailVerified: newUser.isEmailVerified,
        isAdminAuthorized: newUser.isAdminAuthorized
      }
    },
    'Account registered successfully. Verification code sent to email.',
    201
  );
}

export async function login(req: Request, res: Response): Promise<void> {
  const { email, password } = req.body;
  const normalizedEmail = email.trim().toLowerCase();

  const user = await prisma.user.findUnique({
    where: { email: normalizedEmail },
    include: {
      profile: {
        select: { fullName: true }
      }
    }
  });

  if (!user || !user.isActive) {
    sendError(res, 'Invalid email or password.', 401, 'INVALID_CREDENTIALS');
    return;
  }

  const isPasswordValid = await comparePassword(password, user.passwordHash);

  if (!isPasswordValid) {
    sendError(res, 'Invalid email or password.', 401, 'INVALID_CREDENTIALS');
    return;
  }

  const token = signToken({
    userId: user.id,
    email: user.email,
    role: user.role,
    isAdminAuthorized: user.isAdminAuthorized
  });

  sendSuccess(
    res,
    {
      token,
      user: {
        userId: user.id,
        email: user.email,
        name: user.profile?.fullName || '',
        role: user.role,
        isEmailVerified: user.isEmailVerified,
        isAdminAuthorized: user.isAdminAuthorized
      }
    },
    'Login successful'
  );
}

export async function logout(req: Request, res: Response): Promise<void> {
  // In a token-based architecture, client clears the token; server confirms
  sendSuccess(res, { loggedOut: true }, 'Successfully logged out.');
}

export async function getCurrentUser(req: Request, res: Response): Promise<void> {
  if (!req.user) {
    sendError(res, 'Unauthenticated request.', 401, 'UNAUTHORIZED');
    return;
  }

  const user = await prisma.user.findUnique({
    where: { id: req.user.userId },
    select: {
      id: true,
      email: true,
      role: true,
      isEmailVerified: true,
      isAdminAuthorized: true,
      createdAt: true,
      profile: {
        select: {
          id: true,
          fullName: true,
          bio: true,
          avatarUrl: true,
          headline: true,
          phone: true,
          location: true,
          website: true,
          github: true,
          linkedin: true,
          aspirantProfile: true,
          studentProfile: {
            include: { institution: true }
          },
          alumniProfile: {
            include: { institution: true }
          },
          adminProfile: true
        }
      },
      mentorProfile: true
    }
  });

  if (!user) {
    sendError(res, 'User profile not found.', 404, 'NOT_FOUND');
    return;
  }

  sendSuccess(res, {
    userId: user.id,
    email: user.email,
    name: user.profile?.fullName || '',
    role: user.role,
    isEmailVerified: user.isEmailVerified,
    isAdminAuthorized: user.isAdminAuthorized,
    createdAt: user.createdAt,
    profile: user.profile,
    mentorProfile: user.mentorProfile
  });
}

export async function sendOtp(req: Request, res: Response): Promise<void> {
  const { email, purpose } = req.body;
  const normalizedEmail = email.trim().toLowerCase();

  const result = await otpService.sendOtp(normalizedEmail, purpose);

  if (!result.success) {
    if (result.cooldownRemainingSeconds) {
      sendError(res, result.message, 429, 'RATE_LIMITED');
      return;
    }
    sendError(res, result.message, 400, 'OTP_SEND_FAILED');
    return;
  }

  sendSuccess(res, { sent: true }, 'Verification code sent.');
}

export async function verifyOtp(req: Request, res: Response): Promise<void> {
  const { email, otp, purpose } = req.body;
  const normalizedEmail = email.trim().toLowerCase();

  const result = await otpService.verifyOtp(normalizedEmail, otp, purpose);

  if (!result.success) {
    sendError(res, result.message, 400, 'INVALID_OTP');
    return;
  }

  sendSuccess(res, { isEmailVerified: true }, 'Verification successful.');
}

export async function forgotPassword(req: Request, res: Response): Promise<void> {
  const { email } = req.body;
  const normalizedEmail = email.trim().toLowerCase();

  const user = await prisma.user.findUnique({
    where: { email: normalizedEmail }
  });

  // Do not reveal whether user exists for security enumeration protection
  if (user) {
    await otpService.sendOtp(normalizedEmail, 'PASSWORD_RESET');
  }

  sendSuccess(
    res,
    { sent: true },
    'If an account with this email exists, a password reset code has been sent.'
  );
}

export async function resetPassword(req: Request, res: Response): Promise<void> {
  const { email, otp, newPassword } = req.body;
  const normalizedEmail = email.trim().toLowerCase();

  const newPasswordHash = await hashPassword(newPassword);
  const result = await otpService.resetPasswordWithOtp(normalizedEmail, otp, newPasswordHash);

  if (!result.success) {
    sendError(res, result.message, 400, 'PASSWORD_RESET_FAILED');
    return;
  }

  sendSuccess(res, { reset: true }, result.message);
}
