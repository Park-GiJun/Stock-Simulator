package com.stocksimulator.userservice.application.handler.user

import com.stocksimulator.common.exception.DuplicateResourceException
import com.stocksimulator.common.exception.ErrorCode
import com.stocksimulator.userservice.application.dto.command.user.SignUpCommand
import com.stocksimulator.userservice.application.port.out.balance.BalancePersistencePort
import com.stocksimulator.userservice.application.port.out.user.UserPersistencePort
import com.stocksimulator.userservice.domain.BalanceModel
import com.stocksimulator.userservice.domain.UserModel
import com.stocksimulator.userservice.domain.enums.UserRole
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant

@DisplayName("UserCommandHandler ?¨ìœ„ ?ŒìŠ¤??)
class UserCommandHandlerTest {

    // Mocks
    private lateinit var userPersistencePort: UserPersistencePort
    private lateinit var balancePersistencePort: BalancePersistencePort
    private lateinit var passwordEncoder: PasswordEncoder

    // SUT (System Under Test)
    private lateinit var userCommandHandler: UserCommandHandler

    @BeforeEach
    fun setUp() {
        userPersistencePort = mockk()
        balancePersistencePort = mockk()
        passwordEncoder = mockk()

        userCommandHandler = UserCommandHandler(
            userPersistencePort = userPersistencePort,
            balancePersistencePort = balancePersistencePort,
            passwordEncoder = passwordEncoder
        )
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    // ========== ?Œì›ê°€???±ê³µ ?œë‚˜ë¦¬ì˜¤ ==========

    @Test
    @DisplayName("?Œì›ê°€???±ê³µ - ? ì??€ ì´ˆê¸° ?”ê³ (500ë§Œì›) ?ì„±")
    fun `signUp should create user and initial balance successfully`() {
        // given
        val command = SignUpCommand(
            email = "test@example.com",
            username = "?ŒìŠ¤??,
            password = "password123!"
        )

        val encryptedPassword = "encrypted_password"
        val savedUserId = 123L  // Long ?€??

        // Mock: ?´ë©”??ì¤‘ë³µ ?†ìŒ
        every { userPersistencePort.findByEmail(command.email) } returns null

        // Mock: ?‰ë„¤??ì¤‘ë³µ ?†ìŒ
        every { userPersistencePort.findByUsername(command.username) } returns null

        // Mock: ë¹„ë?ë²ˆí˜¸ ?”í˜¸??
        every { passwordEncoder.encode(command.password) } returns encryptedPassword

        // Mock: User ?€??
        every { userPersistencePort.save(any()) } answers {
            val user = firstArg<UserModel>()
            user.withId(savedUserId)
        }

        // Mock: Balance ?€??
        every { balancePersistencePort.save(any()) } answers {
            val balance = firstArg<BalanceModel>()
            balance.copy(balanceId = 456L)  // Long ?€??
        }

        // when
        val result = userCommandHandler.signUp(command)

        // then
        assertThat(result).isNotNull
        assertThat(result.userId).isEqualTo(savedUserId)
        assertThat(result.email).isEqualTo(command.email)
        assertThat(result.username).isEqualTo(command.username)

        // verify: ?´ë©”??ì¤‘ë³µ ê²€ì¦??¸ì¶œ
        verify(exactly = 1) { userPersistencePort.findByEmail(command.email) }

        // verify: ?‰ë„¤??ì¤‘ë³µ ê²€ì¦??¸ì¶œ
        verify(exactly = 1) { userPersistencePort.findByUsername(command.username) }

        // verify: ë¹„ë?ë²ˆí˜¸ ?”í˜¸???¸ì¶œ
        verify(exactly = 1) { passwordEncoder.encode(command.password) }

        // verify: User ?€???¸ì¶œ
        verify(exactly = 1) {
            userPersistencePort.save(
                match {
                    it.email == command.email &&
                    it.username == command.username &&
                    it.password == encryptedPassword &&
                    it.role == UserRole.ROLE_USER &&
                    it.userId == null // ?€???„ì—??ID ?†ìŒ
                }
            )
        }

        // verify: Balance ?€???¸ì¶œ (ì´ˆê¸° 500ë§Œì›)
        verify(exactly = 1) {
            balancePersistencePort.save(
                match {
                    it.userId == savedUserId &&
                    it.cash == 5_000_000L &&
                    it.totalAsset == 5_000_000L &&
                    it.stockValue == 0L &&
                    it.balanceId == null // ?€???„ì—??ID ?†ìŒ
                }
            )
        }
    }

    @Test
    @DisplayName("?Œì›ê°€???±ê³µ - ë¹„ë?ë²ˆí˜¸ê°€ ?”í˜¸?”ë˜???€?¥ë¨")
    fun `signUp should encrypt password before saving`() {
        // given
        val command = SignUpCommand(
            email = "test@example.com",
            username = "?ŒìŠ¤??,
            password = "plain_password"
        )

        val encryptedPassword = "\$2a\$10\$encrypted_hash"

        every { userPersistencePort.findByEmail(any()) } returns null
        every { userPersistencePort.findByUsername(any()) } returns null
        every { passwordEncoder.encode(command.password) } returns encryptedPassword
        every { userPersistencePort.save(any()) } answers {
            firstArg<UserModel>().withId(123L)
        }
        every { balancePersistencePort.save(any()) } answers {
            firstArg<BalanceModel>().copy(balanceId = 456L)
        }

        // when
        userCommandHandler.signUp(command)

        // then
        verify {
            userPersistencePort.save(
                match {
                    it.password == encryptedPassword &&
                    it.password != command.password // ?‰ë¬¸ê³??¤ë¦„
                }
            )
        }
    }

    // ========== ?´ë©”??ì¤‘ë³µ ?œë‚˜ë¦¬ì˜¤ ==========

    @Test
    @DisplayName("?Œì›ê°€???¤íŒ¨ - ?´ë©”??ì¤‘ë³µ")
    fun `signUp should throw DuplicateResourceException when email already exists`() {
        // given
        val command = SignUpCommand(
            email = "duplicate@example.com",
            username = "?ŒìŠ¤??,
            password = "password123!"
        )

        val existingUser = UserModel.of(
            userId = 1L,  // Long ?€??
            email = command.email,
            username = "ê¸°ì¡´? ì?",
            password = "encrypted",
            role = UserRole.ROLE_USER,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        // Mock: ?´ë©”??ì¤‘ë³µ
        every { userPersistencePort.findByEmail(command.email) } returns existingUser

        // when & then
        val exception = assertThrows<DuplicateResourceException> {
            userCommandHandler.signUp(command)
        }

        assertThat(exception.errorCode).isEqualTo(ErrorCode.DUPLICATE_EMAIL)
        assertThat(exception.message).contains("?´ë? ?¬ìš© ì¤‘ì¸ ?´ë©”??)

        // verify: ?´ë©”??ì¤‘ë³µ ê²€ì¦ë§Œ ?¸ì¶œ?˜ê³ , ?˜ë¨¸ì§€???¸ì¶œ ?ˆë¨
        verify(exactly = 1) { userPersistencePort.findByEmail(command.email) }
        verify(exactly = 0) { userPersistencePort.findByUsername(any()) }
        verify(exactly = 0) { passwordEncoder.encode(any()) }
        verify(exactly = 0) { userPersistencePort.save(any()) }
        verify(exactly = 0) { balancePersistencePort.save(any()) }
    }

    // ========== ?‰ë„¤??ì¤‘ë³µ ?œë‚˜ë¦¬ì˜¤ ==========

    @Test
    @DisplayName("?Œì›ê°€???¤íŒ¨ - ?‰ë„¤??ì¤‘ë³µ")
    fun `signUp should throw DuplicateResourceException when username already exists`() {
        // given
        val command = SignUpCommand(
            email = "test@example.com",
            username = "ì¤‘ë³µ?‰ë„¤??,
            password = "password123!"
        )

        val existingUser = UserModel.of(
            userId = 2L,  // Long ?€??
            email = "other@example.com",
            username = command.username,
            password = "encrypted",
            role = UserRole.ROLE_USER,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        // Mock: ?´ë©”?¼ì? ì¤‘ë³µ ?†ìŒ
        every { userPersistencePort.findByEmail(command.email) } returns null

        // Mock: ?‰ë„¤??ì¤‘ë³µ
        every { userPersistencePort.findByUsername(command.username) } returns existingUser

        // when & then
        val exception = assertThrows<DuplicateResourceException> {
            userCommandHandler.signUp(command)
        }

        assertThat(exception.errorCode).isEqualTo(ErrorCode.DUPLICATE_NICKNAME)
        assertThat(exception.message).contains("?´ë? ?¬ìš© ì¤‘ì¸ ?‰ë„¤??)

        // verify: ?´ë©”??ê²€ì¦????‰ë„¤??ê²€ì¦ê¹Œì§€ë§??¸ì¶œ
        verify(exactly = 1) { userPersistencePort.findByEmail(command.email) }
        verify(exactly = 1) { userPersistencePort.findByUsername(command.username) }
        verify(exactly = 0) { passwordEncoder.encode(any()) }
        verify(exactly = 0) { userPersistencePort.save(any()) }
        verify(exactly = 0) { balancePersistencePort.save(any()) }
    }

    // ========== ì´ˆê¸° ?”ê³  ?ì„± ê²€ì¦?==========

    @Test
    @DisplayName("?Œì›ê°€???±ê³µ - ì´ˆê¸° ?”ê³ ê°€ ?•í™•??500ë§Œì›?¼ë¡œ ?ì„±??)
    fun `signUp should create initial balance with exactly 5 million KRW`() {
        // given
        val command = SignUpCommand(
            email = "test@example.com",
            username = "?ŒìŠ¤??,
            password = "password123!"
        )

        every { userPersistencePort.findByEmail(any()) } returns null
        every { userPersistencePort.findByUsername(any()) } returns null
        every { passwordEncoder.encode(any()) } returns "encrypted"
        every { userPersistencePort.save(any()) } answers {
            firstArg<UserModel>().withId(123L)
        }

        val capturedBalance = slot<BalanceModel>()
        every { balancePersistencePort.save(capture(capturedBalance)) } answers {
            firstArg<BalanceModel>().copy(balanceId = 456L)
        }

        // when
        userCommandHandler.signUp(command)

        // then
        val savedBalance = capturedBalance.captured
        assertThat(savedBalance.userId).isEqualTo(123L)
        assertThat(savedBalance.cash).isEqualTo(5_000_000L)
        assertThat(savedBalance.totalAsset).isEqualTo(5_000_000L)
        assertThat(savedBalance.stockValue).isEqualTo(0L)
    }

    // ========== ?¸ëœ??…˜ ë¡¤ë°± ?œë‚˜ë¦¬ì˜¤ (?¤íŒ¨ ?? ==========

    @Test
    @DisplayName("?Œì›ê°€???¤íŒ¨ - User ?€????Balance ?€???¤íŒ¨ ???ˆì™¸ ë°œìƒ")
    fun `signUp should throw exception when balance save fails`() {
        // given
        val command = SignUpCommand(
            email = "test@example.com",
            username = "?ŒìŠ¤??,
            password = "password123!"
        )

        every { userPersistencePort.findByEmail(any()) } returns null
        every { userPersistencePort.findByUsername(any()) } returns null
        every { passwordEncoder.encode(any()) } returns "encrypted"
        every { userPersistencePort.save(any()) } answers {
            firstArg<UserModel>().withId(123L)
        }

        // Mock: Balance ?€???¤íŒ¨
        every { balancePersistencePort.save(any()) } throws RuntimeException("DB ?°ê²° ?¤íŒ¨")

        // when & then
        assertThrows<RuntimeException> {
            userCommandHandler.signUp(command)
        }

        // verify: User ?€?¥ê¹Œì§€???¸ì¶œ??
        verify(exactly = 1) { userPersistencePort.save(any()) }
        verify(exactly = 1) { balancePersistencePort.save(any()) }
    }

    // ========== ê²½ê³„ê°??ŒìŠ¤??==========

    @Test
    @DisplayName("?Œì›ê°€???±ê³µ - ?‰ë„¤??ìµœì†Œ ê¸¸ì´ (2??")
    fun `signUp should succeed with minimum username length`() {
        // given
        val command = SignUpCommand(
            email = "test@example.com",
            username = "AB",  // ìµœì†Œ 2??
            password = "password123!"
        )

        every { userPersistencePort.findByEmail(any()) } returns null
        every { userPersistencePort.findByUsername(any()) } returns null
        every { passwordEncoder.encode(any()) } returns "encrypted"
        every { userPersistencePort.save(any()) } answers {
            firstArg<UserModel>().withId(123L)
        }
        every { balancePersistencePort.save(any()) } answers {
            firstArg<BalanceModel>().copy(balanceId = 456L)
        }

        // when & then (?ˆì™¸ ?†ì´ ?±ê³µ)
        val result = userCommandHandler.signUp(command)
        assertThat(result.username).isEqualTo("AB")
    }

    @Test
    @DisplayName("?Œì›ê°€???±ê³µ - ?‰ë„¤??ìµœë? ê¸¸ì´ (20??")
    fun `signUp should succeed with maximum username length`() {
        // given
        val command = SignUpCommand(
            email = "test@example.com",
            username = "A".repeat(20),  // ìµœë? 20??
            password = "password123!"
        )

        every { userPersistencePort.findByEmail(any()) } returns null
        every { userPersistencePort.findByUsername(any()) } returns null
        every { passwordEncoder.encode(any()) } returns "encrypted"
        every { userPersistencePort.save(any()) } answers {
            firstArg<UserModel>().withId(123L)
        }
        every { balancePersistencePort.save(any()) } answers {
            firstArg<BalanceModel>().copy(balanceId = 456L)
        }

        // when & then (?ˆì™¸ ?†ì´ ?±ê³µ)
        val result = userCommandHandler.signUp(command)
        assertThat(result.username).hasSize(20)
    }

    @Test
    @DisplayName("?Œì›ê°€???¤íŒ¨ - ?‰ë„¤?„ì´ ?ˆë¬´ ì§§ìŒ (1??")
    fun `signUp should fail when username is too short`() {
        // given
        val command = SignUpCommand(
            email = "test@example.com",
            username = "A",  // 1??(ìµœì†Œ 2??ë¯¸ë§Œ)
            password = "password123!"
        )

        every { userPersistencePort.findByEmail(any()) } returns null
        every { userPersistencePort.findByUsername(any()) } returns null

        // when & then
        assertThrows<IllegalArgumentException> {
            userCommandHandler.signUp(command)
        }
    }

    @Test
    @DisplayName("?Œì›ê°€???¤íŒ¨ - ?‰ë„¤?„ì´ ?ˆë¬´ ê¹€ (21??")
    fun `signUp should fail when username is too long`() {
        // given
        val command = SignUpCommand(
            email = "test@example.com",
            username = "A".repeat(21),  // 21??(ìµœë? 20??ì´ˆê³¼)
            password = "password123!"
        )

        every { userPersistencePort.findByEmail(any()) } returns null
        every { userPersistencePort.findByUsername(any()) } returns null

        // when & then
        assertThrows<IllegalArgumentException> {
            userCommandHandler.signUp(command)
        }
    }
}
