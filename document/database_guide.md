users/
└── [userId] (Document ID - Auto)
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
└── [storyId] (Document ID - Auto)
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
