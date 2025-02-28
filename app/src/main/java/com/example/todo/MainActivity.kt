package com.example.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.room.Room
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app-database").build()

        setContent {
            var selectedUser by remember { mutableStateOf<User?>(null) }
            var newItem by remember { mutableStateOf("") }
            var newUser by remember { mutableStateOf("") }
            var users by remember { mutableStateOf<List<User>>(emptyList()) }
            var items by remember { mutableStateOf<List<Item>>(emptyList()) }
            val scope = rememberCoroutineScope()
            var tags by remember { mutableStateOf<List<Tag>>(emptyList()) }
            var selectedTags by remember { mutableStateOf<MutableSet<Long>>(mutableSetOf()) }


            LaunchedEffect(Unit) {
                scope.launch {
                    users = db.userDao().getAllUsers()

                    val existingTags = db.tagDao().getAllTags()
                    if (existingTags.isEmpty()) {
                        initialTags.forEach { tagName ->
                            db.tagDao().insert(Tag(tagName = tagName))
                        }
                    }
                    tags = db.tagDao().getAllTags()
                }
            }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.h4,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // kullanici silme butonu
                    selectedUser?.let {
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        // Kullanıcı ile iliskili UserItem verilerini silme
                                        db.userItemDao().deleteUserItemByUserId(it.id)
                                        db.userDao().deleteUser(it)

                                        users = db.userDao().getAllUsers()
                                        selectedUser = null
                                    } catch (e: Exception) {
                                        println("Kullanici Silinemedi!!!: ${e.message}")
                                    }
                                }
                            },
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Kullanici Sil")
                        }
                    }
                }


                // Kullanıcı Ekleme Alanı
                Text("Kullanici Ekle", modifier = Modifier.padding(bottom = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BasicTextField(
                        value = newUser,
                        onValueChange = { newUser = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                            .border(1.dp, Color.Gray)
                            .padding(16.dp)
                            .height(40.dp)
                    )
                    Button(onClick = {
                        scope.launch {
                            if (newUser.isNotBlank()) {
                                db.userDao().insert(User(username = newUser))
                                users = db.userDao().getAllUsers()
                                newUser = ""
                            }
                        }
                    }) {
                        Text("Kullanici Ekle")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // user Listesi
                Text("Kullanicilar", modifier = Modifier.padding(bottom = 8.dp))
                users.forEach { user ->
                    Text(
                        text = user.username,
                        modifier = Modifier
                            .clickable {
                                selectedUser = user
                                scope.launch {
                                    items = db.userItemDao().getItemsForUser(user.id)
                                }
                            }
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Secilen Kullanici ve Notlari
                selectedUser?.let { user ->
                    Text("${user.username} icin notlar : ", modifier = Modifier.padding(bottom = 8.dp))

                    items.forEach { item ->
                        var isEditing by remember { mutableStateOf(false) }
                        var editedText by remember { mutableStateOf(item.name) }

                        // Share butonunun gosterilmesi ve checklist'in acilmasi
                        var showChecklist by remember { mutableStateOf(false) }

                        // onceki paylasimlarini almak
                        val sharedUsers = remember { mutableStateListOf<User>() }
                        LaunchedEffect(item.id) {
                            sharedUsers.clear()
                            val sharedItemUsers = db.userItemDao().getUsersForItem(item.id)
                            sharedUsers.addAll(sharedItemUsers)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (isEditing) {
                                BasicTextField(
                                    value = editedText,
                                    onValueChange = { editedText = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp)
                                        .border(1.dp, Color.Gray)
                                        .padding(16.dp)
                                        .height(40.dp)
                                )
                                Button(onClick = {
                                    scope.launch {
                                        if (item.createdBy == user.id|| sharedUsers.contains(user)) { // Yetki kontrolü
                                            db.itemDao().updateItem(item.copy(name = editedText))
                                            items = db.userItemDao().getItemsForUser(user.id)
                                        }
                                    }
                                    isEditing = false
                                }, modifier = Modifier.height(40.dp)) {
                                    Text("Kaydet")
                                }
                            } else {
                                Text(item.name, modifier = Modifier.weight(1f))
                                if (item.createdBy == user.id || sharedUsers.contains(user)) { // Yetki kontrolü
                                    Button(onClick = { isEditing = true }, modifier = Modifier.height(40.dp)) {
                                        Text("Duzenle")
                                    }
                                }
                            }



                            if (item.createdBy == user.id) { // Sadece olusturucu silebilir
                                Button(onClick = {
                                    scope.launch {
                                        try {
                                            // Kullanıcı ile iliskili UserItem verilerini silme
                                            db.userItemDao().deleteUserItemByItemId(item.id)
                                            db.itemDao().deleteItem(item.id)

                                            // Silme sonrası item'ları guncelle
                                            items = db.userItemDao().getItemsForUser(user.id)
                                        } catch (e: Exception) {
                                            println("Not Silinemedi!!!: ${e.message}")
                                        }
                                    }
                                }, modifier = Modifier.height(40.dp)) {
                                    Text("Sil")
                                }

                            }
                            if (item.createdBy == user.id) {

                                // Paylaş butonu herkes için erişilebilir
                                Button(onClick = { showChecklist = !showChecklist }, modifier = Modifier.height(40.dp)) {
                                    Text("Paylas")
                            }}
                        }



                        // Paylasma Checklist'i, sadece Share butonuna basildiginda gosterilecek
                        if (showChecklist) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                users.forEach { shareUser ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // onceden paylasilan kullanıcıları isaretle
                                        val isChecked = sharedUsers.contains(shareUser)
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { isChecked ->
                                                if (isChecked) {
                                                    sharedUsers.add(shareUser)
                                                } else {
                                                    sharedUsers.remove(shareUser)
                                                }
                                            }
                                        )
                                        Text(shareUser.username)
                                    }
                                }

                                // Paylasma Butonu
                                //  tiklandiginda
                                Button(onClick = {
                                    scope.launch {
                                        sharedUsers.forEach { shareUser ->
                                            // Kullanıcı ile oge zaten iliskilendirilmiş mi kontrol et
                                            val existingUserItem = db.userItemDao().checkIfUserItemExists(shareUser.id, item.id)

                                            if (existingUserItem == null) { // iliskiyi ekle
                                                db.userItemDao().insert(UserItem(userId = shareUser.id, itemId = item.id))
                                            }
                                        }

                                        // iliskileri guncelle
                                        items = db.userItemDao().getItemsForUser(user.id)
                                    }
                                }, modifier = Modifier.height(40.dp)) {
                                    Text("Paylas")
                                }


                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        Text("Tag Seç", modifier = Modifier.padding(vertical = 8.dp))
                        tags.forEach { tag ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedTags.contains(tag.tagId),
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            selectedTags.add(tag.tagId)
                                        } else {
                                            selectedTags.remove(tag.tagId)
                                        }
                                    }
                                )
                                Text(tag.tagName, modifier = Modifier.padding(start = 8.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Not ekleme alanı
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BasicTextField(
                                value = newItem,
                                onValueChange = { newItem = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp)
                                    .border(1.dp, Color.Gray)
                                    .padding(16.dp)
                                    .height(40.dp)
                            )
                            Button(onClick = {
                                scope.launch {
                                    if (newItem.isNotBlank() && selectedUser != null) {
                                        val item = Item(name = newItem, createdBy = selectedUser!!.id)
                                        val itemId = db.itemDao().insert(item)
                                        db.userItemDao().insert(UserItem(userId = selectedUser!!.id, itemId = itemId))

                                        // tag-not
                                        selectedTags.forEach { tagId ->
                                            db.itemTagDao().insert(ItemTag(itemId = itemId, tagId = tagId))
                                        }

                                        items = db.userItemDao().getItemsForUser(selectedUser!!.id)
                                        newItem = ""
                                        selectedTags.clear()
                                    }
                                }
                            }, modifier = Modifier.height(40.dp)) {
                                Text("Not Ekle")
                            }

                    }


                    }
                }
            }
        }
    }
}