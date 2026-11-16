import { app } from './app';
import { env } from './config/env';
import { prisma } from './services/prisma.service';

const PORT = env.PORT;

async function startServer() {
  try {
    await prisma.$connect();
    console.log('✅ Database connected successfully via Prisma');

    const server = app.listen(PORT, '0.0.0.0', () => {
      console.log(`🚀 CampusVerse Backend API listening on port ${PORT} (0.0.0.0 - all network interfaces)`);
      console.log(`📡 Local Health Check: http://localhost:${PORT}/api/v1/health`);
    });

    const shutdown = async () => {
      console.log('Shutting down server gracefully...');
      server.close(async () => {
        await prisma.$disconnect();
        console.log('Database disconnected. Process exited.');
        process.exit(0);
      });
    };

    process.on('SIGINT', shutdown);
    process.on('SIGTERM', shutdown);
  } catch (error) {
    console.error('Failed to start server:', error);
    process.exit(1);
  }
}

if (require.main === module) {
  startServer();
}

export default app;
