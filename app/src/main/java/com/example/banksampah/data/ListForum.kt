//package com.example.banksampah.data
//
//data class Post(
//    val id: String = "",
//    val author: String = "",
//    val timeAgo: String = "",
//    val title: String = "",
//    val content: String = "",
//    val commentCount: Int = 0
//)
//
//class ForumViewModel : ViewModel() {
//    private val database = FirebaseDatabase.getInstance()
//    private val postsRef = database.getReference("posts")
//
//    private val _posts = MutableStateFlow<List<Post>>(emptyList())
//    val posts = _posts.asStateFlow()
//
//    init {
//        loadPosts()
//    }
//
//    private fun loadPosts() {
//        postsRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val postsList = mutableListOf<Post>()
//                for (postSnapshot in snapshot.children) {
//                    val post = postSnapshot.getValue(Post::class.java)
//                    post?.let {
//                        postsList.add(it.copy(id = postSnapshot.key ?: ""))
//                    }
//                }
//                _posts.value = postsList
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Handle error
//            }
//        })
//    }
//}
