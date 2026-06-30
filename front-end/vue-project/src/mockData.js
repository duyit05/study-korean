// Seed data in Vietnamese for Korean Student Web App
export const defaultUserData = {
  name: "Nguyễn Văn An",
  email: "vanan.student@study-korea.edu.vn",
  avatar: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=256",
  level: "Sơ cấp 1A (Beginner)",
  streak: 5,
  xp: 1250,
  joinedClasses: [
    { id: "class-1", name: "Tiếng Hàn Sơ Cấp 1A", teacher: "Cô Park Ji-yeon", schedule: "Thứ 2, 4, 6 (18:00 - 19:30)", room: "Online Zoom A1" },
    { id: "class-2", name: "Luyện phát âm & Giao tiếp phản xạ", teacher: "Thầy Lee Min-ho", schedule: "Thứ 7 (14:00 - 16:00)", room: "Phòng học 302" }
  ],
  notifications: [
    { id: "notif-1", title: "Thay đổi lịch học lớp Giao tiếp", content: "Lịch học Thứ Bảy tuần này (04/07) sẽ chuyển sang buổi chiều từ 14:00 - 16:00 do Thầy Lee bận họp chuyên môn đột xuất. Các em lưu ý đi học đầy đủ nhé!", date: "2026-06-29T10:30:00", sender: "Thầy Lee Min-ho", isRead: false },
    { id: "notif-2", title: "Bài tập mới được giao - Từ vựng Bài 5", content: "Cô vừa giao 1 bài quiz trắc nghiệm từ vựng và ngữ pháp của Bài 5: Thời tiết và Mùa. Hạn nộp là trước 23:00 Chủ Nhật tuần này. Chúc các em học tốt!", date: "2026-06-28T15:00:00", sender: "Cô Park Ji-yeon", isRead: false },
    { id: "notif-3", title: "Tài liệu học tập bổ sung Bài 4", content: "Cô đã đăng tải file PDF tổng hợp các biểu hiện thường dùng khi đi mua sắm tại chợ truyền thống Hàn Quốc trong phần tài liệu lớp. Các em tải về điện thoại xem nhé.", date: "2026-06-25T08:00:00", sender: "Cô Park Ji-yeon", isRead: true }
  ]
};

export const defaultStudySets = [
  {
    id: "set-1",
    name: "Bài 5: Thời tiết & Mùa (날씨와 계절)",
    description: "Bộ từ vựng cơ bản về các kiểu thời tiết và 4 mùa trong năm tại Hàn Quốc.",
    classId: "class-1",
    words: [
      { id: "w-1-1", word: "봄", pronunciation: "bom", meaning: "Mùa xuân", example: "봄에는 꽃이 피어요.", exampleMeaning: "Hoa nở vào mùa xuân.", status: "review" },
      { id: "w-1-2", word: "여름", pronunciation: "yeo-reum", meaning: "Mùa hè", example: "여름은 아주 덥습니다.", exampleMeaning: "Mùa hè rất nóng.", status: "learned" },
      { id: "w-1-3", word: "가을", pronunciation: "ga-eul", meaning: "Mùa thu", example: "가을에는 단풍이 예뻐요.", exampleMeaning: "Vào mùa thu lá phong đỏ rất đẹp.", status: "unlearned" },
      { id: "w-1-4", word: "겨울", pronunciation: "gyeo-ul", meaning: "Mùa đông", example: "겨울에는 눈이 옵니다.", exampleMeaning: "Tuyết rơi vào mùa đông.", status: "unlearned" },
      { id: "w-1-5", word: "날씨", pronunciation: "nal-ssi", meaning: "Thời tiết", example: "오늘 날씨가 어때요?", exampleMeaning: "Thời tiết hôm nay thế nào?", status: "learned" },
      { id: "w-1-6", word: "덥다", pronunciation: "deop-da", meaning: "Nóng", example: "방 안이 너무 더워요.", exampleMeaning: "Trong phòng nóng quá.", status: "unlearned" },
      { id: "w-1-7", word: "춥다", pronunciation: "chup-da", meaning: "Lạnh", example: "날씨가 추워서 코트를 입었어요.", exampleMeaning: "Thời tiết lạnh nên tôi đã mặc áo khoác.", status: "unlearned" },
      { id: "w-1-8", word: "따뜻하다", pronunciation: "tta-tteut-hada", meaning: "Ấm áp", example: "봄 날씨는 참 따뜻해요.", exampleMeaning: "Thời tiết mùa xuân thật ấm áp.", status: "learned" },
      { id: "w-1-9", word: "시원하다", pronunciation: "si-won-hada", meaning: "Mát mẻ", example: "바람이 불어서 시원해요.", exampleMeaning: "Gió thổi nên rất mát mẻ.", status: "review" }
    ]
  },
  {
    id: "set-2",
    name: "Bài 6: Hoạt động hàng ngày (일상 생활)",
    description: "Từ vựng về các hành động thường ngày, thời gian và địa điểm quen thuộc.",
    classId: "class-1",
    words: [
      { id: "w-2-1", word: "일어나다", pronunciation: "il-eo-na-da", meaning: "Thức dậy", example: "아침 일찍 일어납니다.", exampleMeaning: "Tôi thức dậy sớm vào buổi sáng.", status: "learned" },
      { id: "w-2-2", word: "세수하다", pronunciation: "se-su-hada", meaning: "Rửa mặt", example: "화장실에서 세수를 해요.", exampleMeaning: "Tôi rửa mặt trong nhà vệ sinh.", status: "unlearned" },
      { id: "w-2-3", word: "밥을 먹다", pronunciation: "babeul meok-da", meaning: "Ăn cơm", example: "가족과 함께 아침 밥을 먹어요.", exampleMeaning: "Tôi ăn cơm sáng cùng với gia đình.", status: "learned" },
      { id: "w-2-4", word: "회사에 가다", pronunciation: "hoe-sa-e ga-da", meaning: "Đi đến công ty", example: "8시에 버스를 타고 회사에 갑니다.", exampleMeaning: "Tôi đi xe buýt đến công ty lúc 8 giờ.", status: "unlearned" },
      { id: "w-2-5", word: "일하다", pronunciation: "il-hada", meaning: "Làm việc", example: "오전 9시부터 오후 6시까지 일해요.", exampleMeaning: "Tôi làm việc từ 9 giờ sáng đến 6 giờ chiều.", status: "unlearned" },
      { id: "w-2-6", word: "공부하다", pronunciation: "gong-bu-hada", meaning: "Học tập", example: "도서관에서 한국어를 공부합니다.", exampleMeaning: "Tôi học tiếng Hàn ở thư viện.", status: "learned" },
      { id: "w-2-7", word: "운동하다", pronunciation: "un-dong-hada", meaning: "Tập thể dục", example: "공원에서 운동을 합니다.", exampleMeaning: "Tôi tập thể dục ở công viên.", status: "unlearned" },
      { id: "w-2-8", word: "자다", pronunciation: "ja-da", meaning: "Ngủ", example: "밤 11시에 잡니다.", exampleMeaning: "Tôi đi ngủ lúc 11 giờ đêm.", status: "learned" }
    ]
  }
];

export const defaultQuizzes = [
  {
    id: "quiz-1",
    title: "Trắc nghiệm Bài 5: Thời tiết và Mùa",
    classId: "class-1",
    dueDate: "2026-07-05T23:00:00",
    status: "not_started", // not_started, in_progress, completed
    points: 10,
    score: null,
    timeLimit: 15,
    completedAt: null,
    questions: [
      {
        id: "q-1-1",
        type: "choice",
        question: "Từ nào sau đây có nghĩa là 'Mùa đông'?",
        options: ["봄 (Mùa xuân)", "여름 (Mùa hè)", "가을 (Mùa thu)", "겨울 (Mùa đông)"],
        correctAnswer: "겨울 (Mùa đông)",
        explanation: "'겨울' là Mùa đông. '봄' là Mùa xuân, '여름' là Mùa hè, và '가을' là Mùa thu."
      },
      {
        id: "q-1-2",
        type: "choice",
        question: "Hãy chọn từ trái nghĩa với '춥다' (Lạnh):",
        options: ["덥다 (Nóng)", "시원하다 (Mát mẻ)", "따뜻하다 (Ấm áp)", "맑다 (Trong lành)"],
        correctAnswer: "덥다 (Nóng)",
        explanation: "Từ trái nghĩa của '춥다' (Lạnh) là '덥다' (Nóng). '따뜻하다' là ấm áp, '시원하다' là mát mẻ."
      },
      {
        id: "q-1-3",
        type: "fill",
        question: "Điền từ thích hợp vào chỗ trống để nói 'Thời tiết hôm nay tốt': 오늘 날씨가 _______.",
        correctAnswer: "좋아요",
        explanation: "'좋다' (tốt/đẹp) chia đuôi 아/어/여 요 thành '좋아요'. Câu hoàn chỉnh là: '오늘 날씨가 좋아요.'"
      },
      {
        id: "q-1-4",
        type: "choice",
        question: "Vào mùa nào thì hoa nở nhiều? Hãy điền vào chỗ trống: '봄에는 꽃이 ___.'",
        options: ["와요 (đến)", "피어요 (nở)", "내려요 (rơi xuống)", "불어요 (thổi)"],
        correctAnswer: "피어요 (nở)",
        explanation: "Cụm động từ '꽃이 피다' có nghĩa là hoa nở. Chia đuôi 요 thành '피어요'."
      },
      {
        id: "q-1-5",
        type: "choice",
        question: "Dịch câu sau sang tiếng Việt: '겨울에는 눈이 옵니다.'",
        options: [
          "Mùa hè trời mưa nhiều.",
          "Vào mùa đông, tuyết rơi.",
          "Mùa xuân thời tiết ấm áp.",
          "Mùa thu có lá phong rơi."
        ],
        correctAnswer: "Vào mùa đông, tuyết rơi.",
        explanation: "'겨울' (mùa đông), '눈이 오다' (tuyết rơi). Trọn câu: 'Vào mùa đông, tuyết rơi.'"
      }
    ]
  },
  {
    id: "quiz-2",
    title: "Bài tập: Hoạt động hàng ngày cơ bản",
    classId: "class-1",
    dueDate: "2026-07-08T23:00:00",
    status: "completed",
    points: 10,
    score: 10,
    timeLimit: 20,
    completedAt: "2026-06-29T21:15:00",
    userAnswers: {
      "q-2-1": "일어나다",
      "q-2-2": "공부하다"
    },
    questions: [
      {
        id: "q-2-1",
        type: "choice",
        question: "Hãy chọn động từ đúng nghĩa với 'Thức dậy':",
        options: ["자다 (Ngủ)", "일어나다 (Thức dậy)", "세수하다 (Rửa mặt)", "일하다 (Làm việc)"],
        correctAnswer: "일어나다 (Thức dậy)",
        explanation: "'일어나다' là thức dậy. '자다' là ngủ, '세수하다' là rửa mặt, và '일하다' là làm việc."
      },
      {
        id: "q-2-2",
        type: "choice",
        question: "Hãy chọn động từ nghĩa là 'Học bài/Học tập':",
        options: ["공부하다", "놀다", "쉬다", "운동하다"],
        correctAnswer: "공부하다",
        explanation: "'공부하다' là học tập. '놀다' là chơi, '쉬다' là nghỉ ngơi, '운동하다' là tập thể thao."
      }
    ]
  }
];

export const defaultSchedule = [
  { id: "sch-1", date: "2026-06-29", dayOfWeek: "Thứ Hai", time: "18:00 - 19:30", className: "Tiếng Hàn Sơ Cấp 1A", topic: "Bài 4: Mua sắm (쇼핑) - Luyện đọc", teacher: "Cô Park Ji-yeon", location: "Zoom Online A1", status: "completed" },
  { id: "sch-2", date: "2026-07-01", dayOfWeek: "Thứ Tư", time: "18:00 - 19:30", className: "Tiếng Hàn Sơ Cấp 1A", topic: "Bài 5: Thời tiết & Mùa - Nghe & Nói", teacher: "Cô Park Ji-yeon", location: "Zoom Online A1", status: "upcoming" },
  { id: "sch-3", date: "2026-07-03", dayOfWeek: "Thứ Sáu", time: "18:00 - 19:30", className: "Tiếng Hàn Sơ Cấp 1A", topic: "Bài 5: Thời tiết & Mùa - Ngữ pháp & Viết", teacher: "Cô Park Ji-yeon", location: "Zoom Online A1", status: "upcoming" },
  { id: "sch-4", date: "2026-07-04", dayOfWeek: "Thứ Bảy", time: "14:00 - 16:00", className: "Luyện phát âm & Giao tiếp phản xạ", topic: "Chủ đề 2: Gọi nước tại quán Cafe", teacher: "Thầy Lee Min-ho", location: "Phòng học 302", status: "upcoming" },
  { id: "sch-5", date: "2026-07-06", dayOfWeek: "Thứ Hai", time: "18:00 - 19:30", className: "Tiếng Hàn Sơ Cấp 1A", topic: "Bài 5: Luyện tập tổng hợp", teacher: "Cô Park Ji-yeon", location: "Zoom Online A1", status: "upcoming" }
];
