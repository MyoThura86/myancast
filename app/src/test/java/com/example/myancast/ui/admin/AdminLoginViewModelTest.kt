package com.example.myancast.ui.admin

import com.example.myancast.FakeAdminAuthRepository
import com.example.myancast.data.admin.AdminSignInResult
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminLoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `empty fields show field errors without calling Firebase`() = runTest {
        val auth = FakeAdminAuthRepository()
        val vm = AdminLoginViewModel(auth)

        vm.signIn()
        advanceUntilIdle()

        assertNotNull(vm.state.value.usernameError)
        assertNotNull(vm.state.value.passwordError)
        assertEquals(0, auth.signInCalls)
    }

    @Test
    fun `correct admin credentials sign in and clear the password`() = runTest {
        val vm = AdminLoginViewModel(FakeAdminAuthRepository(password = "secret"))

        vm.onUsernameChange("Admin")
        vm.onPasswordChange("secret")
        vm.signIn()
        advanceUntilIdle()

        assertTrue(vm.state.value.signedIn)
        assertEquals("", vm.state.value.password)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `full admin email also signs in`() = runTest {
        val vm = AdminLoginViewModel(FakeAdminAuthRepository(password = "secret"))

        vm.onUsernameChange("admin@myancast.app")
        vm.onPasswordChange("secret")
        vm.signIn()
        advanceUntilIdle()

        assertTrue(vm.state.value.signedIn)
    }

    @Test
    fun `wrong password shows a Myanmar error`() = runTest {
        val vm = AdminLoginViewModel(FakeAdminAuthRepository(password = "secret"))

        vm.onUsernameChange("admin")
        vm.onPasswordChange("wrong")
        vm.signIn()
        advanceUntilIdle()

        assertFalse(vm.state.value.signedIn)
        assertEquals(AdminSignInResult.WRONG_CREDENTIALS.toMessage(), vm.state.value.error)
    }

    @Test
    fun `network failure is reported`() = runTest {
        val vm = AdminLoginViewModel(FakeAdminAuthRepository(forcedResult = AdminSignInResult.NETWORK_ERROR))

        vm.onUsernameChange("admin")
        vm.onPasswordChange("secret")
        vm.signIn()
        advanceUntilIdle()

        assertEquals(AdminSignInResult.NETWORK_ERROR.toMessage(), vm.state.value.error)
    }

    @Test
    fun `already signed in admin skips the form`() = runTest {
        val vm = AdminLoginViewModel(FakeAdminAuthRepository(signedIn = true))
        assertTrue(vm.state.value.signedIn)
    }

    @Test
    fun `typing clears the previous error`() = runTest {
        val vm = AdminLoginViewModel(FakeAdminAuthRepository())
        vm.onUsernameChange("admin")
        vm.onPasswordChange("bad")
        vm.signIn()
        advanceUntilIdle()

        vm.onPasswordChange("bad2")

        assertEquals(null, vm.state.value.error)
    }
}
