package com.campusverse.app

import com.campusverse.app.data.model.AcademicSummary
import com.campusverse.app.data.model.AiStudyQueryResponse
import com.campusverse.app.data.model.CampusEvent
import com.campusverse.app.data.model.CommunityCommentItem
import com.campusverse.app.data.model.CommunityItem
import com.campusverse.app.data.model.CommunityPostItem
import com.campusverse.app.data.model.CourseItem
import com.campusverse.app.data.model.LibraryResource
import com.campusverse.app.data.model.MarketplaceProduct
import com.campusverse.app.data.model.NoteItem
import com.campusverse.app.data.model.StudentProfileData
import com.campusverse.app.domain.student.StudentRepository
import com.campusverse.app.navigation.Screen
import com.campusverse.app.ui.screens.student.StudentHomeUiState
import com.campusverse.app.ui.screens.student.StudentHomeViewModel
import com.campusverse.app.ui.screens.student.academics.AcademicsUiState
import com.campusverse.app.ui.screens.student.academics.AcademicsViewModel
import com.campusverse.app.ui.screens.student.ai.AiStudyAssistantViewModel
import com.campusverse.app.ui.screens.student.community.CommunityDetailUiState
import com.campusverse.app.ui.screens.student.community.CommunityListUiState
import com.campusverse.app.ui.screens.student.community.CommunityViewModel
import com.campusverse.app.ui.screens.student.events.EventsUiState
import com.campusverse.app.ui.screens.student.events.EventsViewModel
import com.campusverse.app.ui.screens.student.library.LibraryUiState
import com.campusverse.app.ui.screens.student.library.LibraryViewModel
import com.campusverse.app.ui.screens.student.marketplace.MarketplaceListUiState
import com.campusverse.app.ui.screens.student.marketplace.MarketplaceViewModel
import com.campusverse.app.ui.screens.student.marketplace.ProductDetailUiState
import com.campusverse.app.ui.screens.student.notes.NotesUiState
import com.campusverse.app.ui.screens.student.notes.NotesViewModel
import com.campusverse.app.ui.screens.student.profile.StudentProfileUiState
import com.campusverse.app.ui.screens.student.profile.StudentProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Fake in-memory test repository for testing Student module ViewModels.
 */
class FakeStudentRepository : StudentRepository {
    var profileData = StudentProfileData(
        userId = "usr_student_test",
        fullName = "Aarav Sharma",
        email = "student@campusverse.edu",
        role = "STUDENT",
        bio = "Computer science student",
        university = "National Institute of Technology",
        degree = "B.Tech",
        branch = "Computer Science and Engineering",
        semester = 6,
        cgpa = 8.75,
        skills = listOf("Kotlin", "Compose", "DSA")
    )

    var academicSummary = AcademicSummary(
        institutionName = "National Institute of Technology",
        degree = "B.Tech",
        major = "Computer Science and Engineering",
        semester = 6,
        cgpa = 8.75,
        studentIdNumber = "2023CSB1042",
        courses = listOf(
            CourseItem("crs_1", "CS301", "Distributed Systems", 4, "CSE"),
            CourseItem("crs_2", "CS302", "Database Management Systems", 4, "CSE"),
            CourseItem("crs_3", "CS303", "Computer Networks", 3, "CSE")
        )
    )

    val notesList = mutableListOf(
        NoteItem(
            id = "note_1",
            userId = "usr_student_test",
            authorName = "Aarav Sharma",
            courseCode = "CS301",
            title = "Raft Consensus Notes",
            description = "Leader election & log replication",
            fileUrl = "https://docs.campusverse.edu/notes/raft.pdf",
            tags = listOf("raft", "cse"),
            isOwnedByCurrentUser = true
        )
    )

    val libraryList = mutableListOf(
        LibraryResource("lib_1", "CLRS Algorithms 4th Ed", "Cormen", "978-0262046305", "EBOOK", "Shelf A", 5, 3),
        LibraryResource("lib_2", "Computer Networks 5th Ed", "Tanenbaum", "978-0132126953", "PDF", "Shelf B", 2, 1)
    )

    val eventsList = mutableListOf(
        CampusEvent("evt_1", "Campus Hackathon 2026", "36-hr hackathon", "HACKATHON", "Auditorium", false, null, "2026-09-15T09:00:00Z", null, 200, 50, false)
    )

    val communitiesList = mutableListOf(
        CommunityItem("comm_1", "Android Dev Club", "Kotlin & Compose", null, 120, 10, true)
    )

    val postsList = mutableListOf(
        CommunityPostItem("post_1", "comm_1", "usr_student_test", "Aarav", "STUDENT", null, "MVI in Compose", "Details on MVI", 5, 1, null, emptyList(), true)
    )

    val marketplaceList = mutableListOf(
        MarketplaceProduct("mkt_1", "usr_student_test", "Aarav", "STUDENT", null, "Casio Scientific Calculator", "FX-991EX", 650.0, "ELECTRONICS", "LIKE_NEW", emptyList(), "AVAILABLE", null, true)
    )

    override suspend fun getAcademicSummary(): Result<AcademicSummary> = Result.success(academicSummary)
    override suspend fun getCourses(semester: Int?, search: String?): Result<List<CourseItem>> = Result.success(academicSummary.courses.toList())
    override suspend fun getNotes(search: String?, courseId: String?, tag: String?): Result<List<NoteItem>> = Result.success(
        notesList.filter { note ->
            (search == null || note.title.contains(search, ignoreCase = true) || (note.description?.contains(search, ignoreCase = true) == true))
        }
    )
    override suspend fun getMyNotes(): Result<List<NoteItem>> = Result.success(notesList.filter { it.isOwnedByCurrentUser })
    override suspend fun getNoteById(id: String): Result<NoteItem> = Result.success(notesList.first { it.id == id })
    override suspend fun createNote(title: String, description: String?, fileUrl: String, courseId: String?, tags: String?): Result<NoteItem> {
        val n = NoteItem("note_new", "usr_student_test", "Aarav", "CS301", null, title, description, fileUrl, emptyList(), 0, null, true, "PENDING_REVIEW")
        notesList.add(n)
        return Result.success(n)
    }
    override suspend fun updateNote(id: String, title: String?, description: String?, tags: String?): Result<NoteItem> = Result.success(notesList.first())
    override suspend fun deleteNote(id: String): Result<Boolean> {
        notesList.removeAll { it.id == id }
        return Result.success(true)
    }
    override suspend fun requestNoteRemoval(noteId: String, reason: String): Result<NoteItem> {
        val idx = notesList.indexOfFirst { it.id == noteId }
        val updated = notesList[idx].copy(status = "REMOVAL_REQUESTED", removalReason = reason)
        notesList[idx] = updated
        return Result.success(updated)
    }
    override suspend fun reportNote(noteId: String, reason: String): Result<Boolean> = Result.success(true)
    override suspend fun getLibraryResources(search: String?, category: String?): Result<List<LibraryResource>> = Result.success(
        libraryList.filter { res ->
            (category == null || category == "ALL" || res.category.equals(category, ignoreCase = true)) &&
            (search == null || res.title.contains(search, ignoreCase = true) || res.author.contains(search, ignoreCase = true))
        }
    )
    override suspend fun getLibraryResourceById(id: String): Result<LibraryResource> = Result.success(libraryList.first())
    override suspend fun askAiStudyAssistant(query: String, mode: String, topic: String?): Result<AiStudyQueryResponse> = Result.success(
        AiStudyQueryResponse(true, query, mode, null, "Detailed explanation of $query.", listOf("DSA", "OS"))
    )
    override suspend fun getEvents(search: String?, category: String?): Result<List<CampusEvent>> = Result.success(
        eventsList.filter { ev ->
            (category == null || category == "ALL" || ev.category.equals(category, ignoreCase = true)) &&
            (search == null || ev.title.contains(search, ignoreCase = true))
        }
    )
    override suspend fun getEventById(id: String): Result<CampusEvent> = Result.success(eventsList.first())
    override suspend fun registerForEvent(eventId: String): Result<Boolean> {
        val idx = eventsList.indexOfFirst { it.id == eventId }
        if (idx != -1) eventsList[idx] = eventsList[idx].copy(isRegistered = true, registeredCount = eventsList[idx].registeredCount + 1)
        return Result.success(true)
    }
    override suspend fun unregisterFromEvent(eventId: String): Result<Boolean> {
        val idx = eventsList.indexOfFirst { it.id == eventId }
        if (idx != -1) eventsList[idx] = eventsList[idx].copy(isRegistered = false, registeredCount = eventsList[idx].registeredCount - 1)
        return Result.success(true)
    }
    override suspend fun getCommunities(search: String?): Result<List<CommunityItem>> = Result.success(
        communitiesList.filter { c ->
            search == null || c.name.contains(search, ignoreCase = true)
        }
    )
    override suspend fun getCommunityById(id: String): Result<Pair<CommunityItem, List<CommunityPostItem>>> = Result.success(Pair(communitiesList.first(), postsList.toList()))
    override suspend fun joinCommunity(communityId: String): Result<Boolean> {
        val idx = communitiesList.indexOfFirst { it.id == communityId }
        if (idx != -1) communitiesList[idx] = communitiesList[idx].copy(isMember = true)
        return Result.success(true)
    }
    override suspend fun leaveCommunity(communityId: String): Result<Boolean> {
        val idx = communitiesList.indexOfFirst { it.id == communityId }
        if (idx != -1) communitiesList[idx] = communitiesList[idx].copy(isMember = false)
        return Result.success(true)
    }
    override suspend fun createCommunityPost(communityId: String, title: String, content: String): Result<CommunityPostItem> {
        val p = CommunityPostItem("post_new", communityId, "usr_student_test", "Aarav", "STUDENT", null, title, content, 0, 0, null, emptyList(), true)
        postsList.add(p)
        return Result.success(p)
    }
    override suspend fun updateCommunityPost(postId: String, title: String?, content: String?): Result<CommunityPostItem> = Result.success(postsList.first())
    override suspend fun deleteCommunityPost(postId: String): Result<Boolean> {
        postsList.removeAll { it.id == postId }
        return Result.success(true)
    }
    override suspend fun likeCommunityPost(postId: String): Result<Int> = Result.success(6)
    override suspend fun createComment(postId: String, content: String): Result<CommunityCommentItem> = Result.success(CommunityCommentItem("cm_new", postId, "usr_student_test", "Aarav", null, content))
    override suspend fun reportContent(targetType: String, targetId: String, reason: String): Result<Boolean> = Result.success(true)
    override suspend fun getMarketplaceListings(search: String?, category: String?, condition: String?): Result<List<MarketplaceProduct>> = Result.success(
        marketplaceList.filter { p ->
            (category == null || category == "ALL" || p.category.equals(category, ignoreCase = true)) &&
            (search == null || p.title.contains(search, ignoreCase = true))
        }
    )
    override suspend fun getMarketplaceProductById(id: String): Result<MarketplaceProduct> = Result.success(marketplaceList.first())
    override suspend fun createMarketplaceListing(title: String, description: String, price: Double, category: String, condition: String): Result<MarketplaceProduct> {
        val item = MarketplaceProduct("mkt_new", "usr_student_test", "Aarav", "STUDENT", null, title, description, price, category, condition, emptyList(), "AVAILABLE", null, true)
        marketplaceList.add(item)
        return Result.success(item)
    }
    override suspend fun updateMarketplaceListing(id: String, price: Double?, status: String?): Result<MarketplaceProduct> {
        val idx = marketplaceList.indexOfFirst { it.id == id }
        if (idx != -1) {
            marketplaceList[idx] = marketplaceList[idx].copy(
                price = price ?: marketplaceList[idx].price,
                status = status ?: marketplaceList[idx].status
            )
        }
        return Result.success(marketplaceList.first())
    }
    override suspend fun deleteMarketplaceListing(id: String): Result<Boolean> {
        marketplaceList.removeAll { it.id == id }
        return Result.success(true)
    }
    override suspend fun getStudentProfile(): Result<StudentProfileData> = Result.success(profileData)
    override suspend fun updateStudentProfile(data: StudentProfileData): Result<StudentProfileData> {
        profileData = data
        return Result.success(data)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class StudentModuleTests {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeStudentRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeStudentRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testStudentHomeViewModel_loadsDashboardData() = runTest {
        val viewModel = StudentHomeViewModel(fakeRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is StudentHomeUiState.Success)
        val success = state as StudentHomeUiState.Success
        assertEquals("Aarav Sharma", success.profile.fullName)
        assertEquals("National Institute of Technology", success.academics.institutionName)
        assertEquals(3, success.academics.courses.size)
        assertEquals(1, success.upcomingEvents.size)
    }

    @Test
    fun testAcademicsViewModel_filtersCourses() = runTest {
        val viewModel = AcademicsViewModel(fakeRepository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AcademicsUiState.Success)

        viewModel.onSearchQueryChange("Database")
        val state = viewModel.uiState.value as AcademicsUiState.Success
        assertEquals(1, state.filteredCourses.size)
        assertEquals("CS302", state.filteredCourses[0].code)
    }

    @Test
    fun testNotesViewModel_createsAndRequestsRemovalOfNote() = runTest {
        val viewModel = NotesViewModel(fakeRepository)
        advanceUntilIdle()

        var created = false
        viewModel.createNote("Operating Systems Semaphores", "Mutex & Semaphores", "https://docs.campusverse.edu/notes/os.pdf", "os,mutex") {
            created = true
        }
        advanceUntilIdle()
        assertTrue(created)

        viewModel.requestRemoval("note_1", "Outdated material")
        advanceUntilIdle()

        val state = viewModel.uiState.value as NotesUiState.Success
        assertEquals("Removal request submitted for moderation review.", state.actionMessage)

        // Switch to MY_NOTES tab
        viewModel.selectTab(com.campusverse.app.ui.screens.student.notes.StudentNotesTab.MY_NOTES)
        advanceUntilIdle()

        val myNotesState = viewModel.uiState.value as NotesUiState.Success
        assertEquals(com.campusverse.app.ui.screens.student.notes.StudentNotesTab.MY_NOTES, myNotesState.selectedTab)
    }


    @Test
    fun testNotesViewModel_reportsNoteAndClearsMessage() = runTest {
        val viewModel = NotesViewModel(fakeRepository)
        advanceUntilIdle()

        viewModel.reportNote("note_1", "Inappropriate content in notes")
        advanceUntilIdle()

        var state = viewModel.uiState.value as NotesUiState.Success
        assertEquals("Note reported to campus moderation.", state.actionMessage)

        viewModel.clearActionMessage()
        state = viewModel.uiState.value as NotesUiState.Success
        assertNull(state.actionMessage)
    }

    @Test
    fun testLibraryViewModel_categorySelectionAndSearch() = runTest {
        val viewModel = LibraryViewModel(fakeRepository)
        advanceUntilIdle()

        viewModel.setCategory("PDF")
        advanceUntilIdle()

        var state = viewModel.uiState.value as LibraryUiState.Success
        assertEquals("PDF", state.selectedCategory)
        assertEquals(1, state.resources.size)
        assertEquals("Computer Networks 5th Ed", state.resources.first().title)

        viewModel.searchLibrary("Tanenbaum")
        advanceUntilIdle()
        state = viewModel.uiState.value as LibraryUiState.Success
        assertEquals(1, state.resources.size)
    }

    @Test
    fun testAiStudyAssistantViewModel_submitsQueryAndReceivesExplanation() = runTest {
        val viewModel = AiStudyAssistantViewModel(fakeRepository)
        advanceUntilIdle()

        viewModel.submitQuery("Explain Dijkstra Shortest Path Algorithm")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.messages.size >= 2)
        assertEquals("USER", state.messages[1].sender)
        assertEquals("AI", state.messages[2].sender)
        assertTrue(state.messages[2].text.contains("Dijkstra"))
    }

    @Test
    fun testAiStudyAssistantViewModel_modeSelection() = runTest {
        val viewModel = AiStudyAssistantViewModel(fakeRepository)
        advanceUntilIdle()

        viewModel.setMode("SUMMARIZE")
        assertEquals("SUMMARIZE", viewModel.uiState.value.selectedMode)

        viewModel.setMode("CONCEPT_QA")
        assertEquals("CONCEPT_QA", viewModel.uiState.value.selectedMode)
    }

    @Test
    fun testEventsViewModel_registrationToggle() = runTest {
        val viewModel = EventsViewModel(fakeRepository)
        advanceUntilIdle()

        val event = (viewModel.uiState.value as EventsUiState.Success).events.first()
        assertFalse(event.isRegistered)

        viewModel.toggleRegistration(event)
        advanceUntilIdle()

        var updatedEvent = (viewModel.uiState.value as EventsUiState.Success).events.first()
        assertTrue(updatedEvent.isRegistered)
        assertEquals(51, updatedEvent.registeredCount)

        viewModel.toggleRegistration(updatedEvent)
        advanceUntilIdle()

        updatedEvent = (viewModel.uiState.value as EventsUiState.Success).events.first()
        assertFalse(updatedEvent.isRegistered)
        assertEquals(50, updatedEvent.registeredCount)
    }

    @Test
    fun testCommunityViewModel_postCreationAndDetails() = runTest {
        val viewModel = CommunityViewModel(fakeRepository)
        advanceUntilIdle()

        var postCreated = false
        viewModel.createPost("comm_1", "New Android Internship Tips", "DSA + Compose") {
            postCreated = true
        }
        advanceUntilIdle()
        assertTrue(postCreated)

        viewModel.loadCommunityDetails("comm_1")
        advanceUntilIdle()

        val detailState = viewModel.detailState.value as CommunityDetailUiState.Success
        assertEquals("Android Dev Club", detailState.community.name)
        assertTrue(detailState.posts.any { it.title == "New Android Internship Tips" })
    }

    @Test
    fun testCommunityViewModel_joinToggle() = runTest {
        val viewModel = CommunityViewModel(fakeRepository)
        advanceUntilIdle()

        val comm = (viewModel.listState.value as CommunityListUiState.Success).communities.first()
        assertTrue(comm.isMember)

        viewModel.toggleJoinCommunity(comm)
        advanceUntilIdle()

        val updatedComm = (viewModel.listState.value as CommunityListUiState.Success).communities.first()
        assertFalse(updatedComm.isMember)
    }

    @Test
    fun testMarketplaceViewModel_createsListingAndMarksSold() = runTest {
        val viewModel = MarketplaceViewModel(fakeRepository)
        advanceUntilIdle()

        var listed = false
        viewModel.createListing("Operating Systems Textbook", "Hardcover", 500.0, "TEXTBOOK", "GOOD") {
            listed = true
        }
        advanceUntilIdle()
        assertTrue(listed)

        viewModel.markAsSold("mkt_1")
        advanceUntilIdle()

        viewModel.loadProductDetail("mkt_1")
        advanceUntilIdle()

        val product = (viewModel.detailState.value as ProductDetailUiState.Success).product
        assertEquals("SOLD", product.status)
    }

    @Test
    fun testStudentProfileViewModel_validatesAndSavesProfile() = runTest {
        val viewModel = StudentProfileViewModel(fakeRepository)
        advanceUntilIdle()

        // Negative check 1: Empty name
        viewModel.saveProfile("", "bio", "bengaluru", "B.Tech", "CSE", "6", "8.5", listOf("Kotlin"))
        advanceUntilIdle()
        var state = viewModel.uiState.value as StudentProfileUiState.Success
        assertNotNull(state.errorMessage)

        // Negative check 2: Invalid CGPA > 10.0
        viewModel.saveProfile("Aarav", "bio", "bengaluru", "B.Tech", "CSE", "6", "11.5", listOf("Kotlin"))
        advanceUntilIdle()
        state = viewModel.uiState.value as StudentProfileUiState.Success
        assertNotNull(state.errorMessage)

        // Valid Save
        viewModel.saveProfile("Aarav Sharma Updated", "Updated Bio", "Bengaluru", "B.Tech", "CSE", "7", "9.2", listOf("Kotlin", "Jetpack Compose"))
        advanceUntilIdle()
        state = viewModel.uiState.value as StudentProfileUiState.Success
        assertEquals("Aarav Sharma Updated", state.profile.fullName)
        assertEquals(7, state.profile.semester)
        assertEquals(9.2, state.profile.cgpa, 0.001)
        assertEquals("Student profile saved successfully!", state.saveSuccessMessage)
    }
}
