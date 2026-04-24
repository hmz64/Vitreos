package com.rx.vitreos.ui.screens

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rx.vitreos.data.repository.ChatRepository
import com.rx.vitreos.domain.model.Contact
import com.rx.vitreos.domain.model.Message
import com.rx.vitreos.domain.model.MessageStatus
import com.rx.vitreos.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class VitreosViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val dataStore: androidx.datastore.core.DataStore<Preferences>
) : ViewModel() {

    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USERNAME = stringPreferencesKey("username")
    }

    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _currentUsername = MutableStateFlow("")
    private val _currentChatUserId = MutableStateFlow("")

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    val contacts: StateFlow<List<Contact>> = _currentUserId.flatMapLatest { userId ->
        if (userId.isEmpty()) {
            flowOf(emptyList())
        } else {
            repository.getAllUsers().map { users ->
                users.filter { it.userId != userId }.map { user ->
                    Contact(
                        user = user,
                        lastMessage = null,
                        lastMessageTime = null,
                        unreadCount = 0
                    )
                }
            }
        }
    }.map { contacts ->
        contacts.sortedByDescending { it.lastMessageTime ?: 0 }
    }.let { flow ->
        MutableStateFlow(emptyList<Contact>()).also { state ->
            viewModelScope.launch { 
                flow.collect { state.value = it }
            }
        }
    }.let { initial ->
        MutableStateFlow(emptyList<Contact>())
    }

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contactsFlow: StateFlow<List<Contact>> = _contacts.asStateFlow()

    val currentChatMessages: StateFlow<List<Message>> = _currentChatUserId.flatMapLatest { otherUserId ->
        if (otherUserId.isEmpty() || _currentUserId.value.isEmpty()) {
            flowOf(emptyList())
        } else {
            val roomId = generateRoomId(_currentUserId.value, otherUserId)
            repository.getMessagesByRoom(roomId)
        }
    }.let { flow ->
        MutableStateFlow(emptyList<Message>()).also { state ->
            viewModelScope.launch {
                flow.collect { messages ->
                    state.value = messages
                }
            }
        }
    }.let { initial ->
        MutableStateFlow(emptyList<Message>())
    }

    private val _isContactTyping = MutableStateFlow(false)
    val isContactTyping: StateFlow<Boolean> = _isContactTyping.asStateFlow()

    private val _isContactOnline = MutableStateFlow(false)
    val isContactOnline: StateFlow<Boolean> = _isContactOnline.asStateFlow()

    init {
        loadSavedUser()
        observeTypingStatus()
    }

    private fun loadSavedUser() {
        viewModelScope.launch {
            val userId = dataStore.data.first()[KEY_USER_ID] ?: ""
            val username = dataStore.data.first()[KEY_USERNAME] ?: ""

            if (userId.isNotEmpty()) {
                _currentUserId.value = userId
                _currentUsername.value = username
                _isLoggedIn.value = true
                connectToSocket(userId)
                fetchAllUsers()
            }
        }
    }

    private fun observeTypingStatus() {
        viewModelScope.launch {
            repository.typingUsers().collect { typingSet ->
                _isContactTyping.value = typingSet.contains(_currentChatUserId.value)
            }
        }
    }

    fun saveUser(userId: String, username: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_USER_ID] = userId
                prefs[KEY_USERNAME] = username
            }
            _currentUserId.value = userId
            _currentUsername.value = username
            _isLoggedIn.value = true
            connectToSocket(userId)
            fetchAllUsers()
        }
    }

    private fun connectToSocket(userId: String) {
        repository.connect(userId)
    }

    private fun fetchAllUsers() {
        viewModelScope.launch {
            try {
                val url = URL("http://10.0.2.2:3001/auth/users")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                
                val response = connection.inputStream.bufferedReader().readText()
                
                if (response.contains("\"success\":true")) {
                    val users = parseUsers(response)
                    repository.saveUsers(users)
                    _contacts.value = users.filter { 
                        it.userId != _currentUserId.value 
                    }.map { user ->
                        Contact(user = user)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun parseUsers(json: String): List<User> {
        val users = mutableListOf<User>()
        
        try {
            val dataStart = json.indexOf("\"data\":[")
            if (dataStart == -1) return users
            
            val dataEnd = json.indexOf("]", dataStart)
            val dataArray = json.substring(dataStart + 7, dataEnd)
            
            val userRegex = """\{[^}]+\}""".toRegex()
            userRegex.findAll(dataArray).forEach { match ->
                val userJson = match.value
                val userId = userJson.substringAfter("\"userId\":\"").substringBefore("\"")
                val username = userJson.substringAfter("\"username\":\"").substringBefore("\"")
                val phone = userJson.substringAfter("\"phone\":\"").substringBefore("\"")
                
                if (userId.isNotEmpty()) {
                    users.add(User(userId, username, phone))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return users
    }

    fun setCurrentChat(otherUserId: String) {
        _currentChatUserId.value = otherUserId
        val roomId = generateRoomId(_currentUserId.value, otherUserId)
        
        repository.joinRoom(otherUserId)
        loadChatHistory(otherUserId)
        checkOnlineStatus(otherUserId)
    }

    private fun loadChatHistory(otherUserId: String) {
        repository.getHistory(otherUserId) { messages ->
            viewModelScope.launch {
                repository.saveMessages(messages)
            }
        }
    }

    private fun checkOnlineStatus(userId: String) {
        repository.checkOnlineStatus(userId)
    }

    fun sendMessage(to: String, content: String) {
        val localId = "local_${System.currentTimeMillis()}"
        val roomId = generateRoomId(_currentUserId.value, to)
        
        val localMessage = Message(
            id = localId,
            senderId = _currentUserId.value,
            receiverId = to,
            content = content,
            roomId = roomId,
            status = MessageStatus.SENDING,
            timestamp = System.currentTimeMillis()
        )
        
        viewModelScope.launch {
            repository.saveMessage(localMessage)
        }
        
        repository.sendMessage(to, content)
    }

    fun sendTyping(to: String) {
        repository.sendTyping(to)
    }

    fun sendStopTyping(to: String) {
        repository.sendStopTyping(to)
    }

    private fun generateRoomId(userId1: String, userId2: String): String {
        return listOf(userId1, userId2).sorted().joinToString("_")
    }

    override fun onCleared() {
        super.onCleared()
        repository.disconnect()
    }
}