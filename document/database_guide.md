users/
└── [userId] 
    ├── userId: String
    ├── username: String
    ├── email: String
    ├── phoneNumber: String
    ├── avatarUrl: String
    ├── fcmToken: String
    ├── linkedAccounts: Map
    │   ├── google: Boolean
    │   └── facebook: Boolean
    └── createdAt: Timestamp

stories/
└── [storyId] 
    ├── storyId: String
    ├── title: String
    ├── title_lowercase: String 
    ├── author: String
    ├── coverImage: String
    ├── description: String
    ├── status: String
    ├── viewCount: Number 
    ├── dailyViews: Number 
    ├── category: Array [String]
    ├── keywords: Array [String] 
    └── updatedAt: Timestamp
    └── chapters/ (Sub-collection)
        └── [chapterId] 
            ├── chapterId: String
            ├── chapterNumber: Number 
            ├── title: String
            ├── content: String
            └── createdAt: Timestamp

comments/
└── [commentId] 
    ├── commentId: String
    ├── storyId: String 
    ├── userId: String 
    ├── content: String
    ├── createdAt: Timestamp
    ├── userName: String 
    └── userAvatar: String 

bookmarks/
└── [bookmarkId] 
    ├── userId: String
    ├── storyId: String
    ├── lastChapterId: String 
    ├── isFavorite: Boolean 
    └── updatedAt: Timestamp 
