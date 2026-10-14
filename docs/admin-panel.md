# Admin Panel — Setup နဲ့ အသုံးပြုပုံ (Phase 6)

> Admin က app ထဲကနေ (Settings → **စီမံခန့်ခွဲသူ**) Podcast နဲ့ အပိုင်းတွေကို တင်/ပြင်/ဖျက်လို့ရတယ်။
> တင်လိုက်တာနဲ့ အသုံးပြုသူ ဖုန်းတိုင်းမှာ **ချက်ချင်း** ပေါ်တယ် (Firestore snapshot listener — app ပြန်ဖွင့်စရာ မလို)။

---

## ၁။ Firebase Console မှာ တစ်ခါပဲ လုပ်ရမယ့် setup

| # | လုပ်ရမယ့်အရာ | နေရာ |
|---|---|---|
| 1 | **Email/Password** ဝင်ခွင့် ဖွင့် | Authentication → Sign-in method → Email/Password → Enable |
| 2 | Admin အကောင့် ဖန်တီး — Email **`admin@myancast.app`** · Password = အဖွဲ့ သဘောတူထားတဲ့ စကားဝှက် | Authentication → Users → Add user |
| 3 | (အကြံပြု) တခြားသူ အကောင့် ကိုယ်တိုင် မဖွင့်နိုင်အောင် sign-up ပိတ် | Authentication → Settings → User actions |
| 4 | **Firestore rules** — `firebase/firestore.rules` တစ်ခုလုံး ကူးထည့် → Publish | Firestore Database → Rules |
| 5 | **Storage** ဖွင့်ပြီး rules — `firebase/storage.rules` ကူးထည့် → Publish | Storage → Get started → Rules |
| 6 | Composite index (`episodes`: `podcastId` ↑ + `publishedAt` ↓) — Phase 2 ကတည်းက ရှိပြီးသား | Firestore → Indexes |

- App ထဲမှာ username ကို **`admin`** (သို့) email အပြည့် **`admin@myancast.app`** ရိုက်လို့ရတယ်။
- ⚠️ အကောင့်ကို **Authentication → Users** မှာ ဖန်တီးရမယ် — Firestore ထဲမှာ document အဖြစ် ထည့်တာ မဟုတ်ပါ။
- **စကားဝှက်ကို code ထဲမှာ မထည့်ဘူး** — Firebase Console မှာပဲ သတ်မှတ်/ပြောင်းတယ် (Users → ⋮ → Reset password)။
- Admin email ပြောင်းချင်ရင် — `AdminConfig.EMAIL` + `firestore.rules` + `storage.rules` ၃ နေရာလုံး ပြောင်းပါ။

### ⚠️ Firebase Storage နဲ့ Blaze plan

ဒီ project ရဲ့ bucket (`myancast-personal.firebasestorage.app`) က **Blaze (pay-as-you-go) plan** လိုတယ်
(2024 အောက်တိုဘာကနေ စပြီး bucket အသစ်တွေ အတွက် Google ရဲ့ စည်းမျဉ်း)။ Free ပမာဏ အတွင်းဆို ပိုက်ဆံ မကုန်ပေမဲ့
billing account (card) ချိတ်ရတယ် — **Budget alert** ထားပါ။

Blaze မသုံးချင်ရင်လည်း admin panel အလုပ်လုပ်တယ် — **"ပုံ လင့်ခ်" / "MP3 လင့်ခ်"** mode (အင်တာနက်ပေါ်က လင့်ခ်) နဲ့ပဲ
တင်ပါ။ "ဖုန်းထဲက ပုံ / MP3" mode ကတော့ Storage လိုတယ်။

---

## ၂။ အသုံးပြုပုံ

```
Settings → စီမံခန့်ခွဲသူ → admin + စကားဝှက် → ဝင်မည်
  └─ စီမံခန့်ခွဲမှု (Dashboard) — Podcast အားလုံး · "Podcast ၅ ခု · အပိုင်း ၂၄ ခု"
       ├─ Podcast အသစ် (ခလုတ်)  → ပုံ · ခေါင်းစဉ် · အမျိုးအစား · အကြောင်းအရာ → Podcast ထည့်မည်
       └─ Podcast နှိပ်           → ပြင် / ဖျက် · အပိုင်း စာရင်း
            └─ အပိုင်း အသစ်      → MP3 · ခေါင်းစဉ် · ကြာချိန် (အလိုအလျောက်) · ထုတ်လွှင့်ရက် · ပုံ (မထည့်လည်းရ)
```

| လုပ်ဆောင်ချက် | အသေးစိတ် |
|---|---|
| **ပုံ** | ဖုန်းထဲက ပုံ (1080px JPEG အဖြစ် ချုံ့ပြီး တင် — အသုံးပြုသူရဲ့ data သက်သာ) **သို့မဟုတ်** ပုံ လင့်ခ် (preview ပြပြီး ပုံ ဖွင့်မရရင် သိမ်းခွင့် မပေး) |
| **MP3** | ဖုန်းထဲက `.mp3` **သို့မဟုတ်** MP3 လင့်ခ် — ရွေးတာနဲ့ (လင့်ခ်ဆို "စစ်ဆေးမည်" / သိမ်းချိန်) ဖိုင်ကို ဖွင့်စစ်ပြီး **ကြာချိန်၊ အရွယ်အစား** ကို အလိုအလျောက် ဖြည့် · MP3 မဟုတ်ရင် ငြင်း |
| **ကြာချိန်** | `24:30` / `1:02:05` (မြန်မာ ဂဏန်း `၂၄:၃၀` လည်း ရ) — ကိုယ်တိုင် ပြင်ထားရင် MP3 အသစ် ရွေးလည်း မဖုံး |
| **ထုတ်လွှင့်ရက်** | ရက်စွဲ + အချိန် · အနာဂတ် ရက် ရွေးလို့ မရ (app က ချက်ချင်း ပြလို့) |
| **episodeCount** | အပိုင်း ထည့်/ဖျက်တိုင်း **အလိုအလျောက်** ပြင် (Console က လွဲနေတာကိုလည်း ရေတွက်ပြီး မှန်အောင် ပြင်) |
| **ဖျက်** | Podcast ဖျက်ရင် သူ့ အပိုင်းအားလုံး + Storage ထဲက ပုံ/MP3 ပါ ဖျက် · အတည်ပြု dialog ပါ |
| **လုံခြုံမှု** | မသိမ်းရသေးဘဲ ထွက်ရင် မေး · တင်နေတုန်း ထွက်ရင် မေး · Firestore မှာ သိမ်းမရရင် ခုနက တင်လိုက်တဲ့ ဖိုင် ပြန်ဖျက် |
| **Data ပြဿနာ ပြ** | အပိုင်း စာရင်းမှာ "⚠ အသံဖိုင် မရှိ · ကြာချိန် မရှိ" (Console က ထည့်ခဲ့တဲ့ data ပြင်ဖို့) |

---

## ၃။ Firestore ထဲ ဘယ်လို သိမ်းလဲ (field + type အတိအကျ)

Admin က app ဖတ်တဲ့ model (`Podcast`, `Episode`) နဲ့ **field နာမည်တစ်လုံးမှ မလွဲအောင်** ရေးတယ် — `FirestoreSchemaTest` က စစ်တယ်။

| Collection | Field | Type | မှတ်ချက် |
|---|---|---|---|
| `podcasts` | `title`, `description`, `coverUrl`, `category` | string | ရှေ့နောက် space ဖယ် |
| | `episodeCount` | number (integer) | အလိုအလျောက် |
| | `updatedAt` | timestamp | server အချိန် |
| `episodes` | `podcastId` | string | podcast ရဲ့ document ID |
| | `title`, `description`, `audioUrl` | string | |
| | `duration` | number (integer) | **စက္ကန့်** |
| | `publishedAt` | timestamp | |
| | `coverUrl` | string | `""` = podcast ပုံ သုံး |
| | `fileSizeBytes` | number (integer) | ★ Phase 6 · `0` = မသိ (episode row ရဲ့ "~၁၂ MB" အတွက်) |

Storage — `podcasts/<podcastId>/cover_<time>.jpg` · `episodes/<episodeId>/cover_<time>.jpg` · `episodes/<episodeId>/audio_<time>.mp3`
(နာမည်ထဲ အချိန် ပါလို့ ပုံ ပြောင်းရင် URL အသစ် ဖြစ်ပြီး ဖုန်းတွေက cache ထဲက ပုံဟောင်း မပြတော့ဘူး)

---

## ၄။ Architecture

```
ui/admin/      AdminLogin · AdminDashboard · AdminPodcast · AdminEpisode (Screen + ViewModel)
               AdminForm.kt (form state + စစ်ဆေးမှု — pure, test လုပ်ထား)
data/admin/    AdminAuthRepository      → FirebaseAdminAuthRepository   (Firebase Auth)
               AdminContentRepository   → FirestoreAdminContentRepository (Firestore ရေး — batch)
               MediaUploader            → FirebaseStorageUploader        (Storage · progress)
               AudioInspector           → AndroidAudioInspector          (MediaMetadataRetriever)
               FirestoreSchema.kt (field နာမည် + map)
```

- ViewModel တွေက Firebase / `android.*` ကို မသိဘူး — interface ပဲ သိ (test မှာ fake: `FakeAdminRepositories.kt`)။
- Error တိုင်း `AdminException` (မြန်မာ message) — ViewModel က snackbar နဲ့ ပြ။
- ဖတ်တာက အသုံးပြုသူနဲ့ တူတူ `PodcastRepository` — admin မြင်တာ = အသုံးပြုသူ မြင်တာ။

---

## ၅။ ပြဿနာ ဖြေရှင်းနည်း

| App က ပြတဲ့ စာ | ဖြေရှင်းနည်း |
|---|---|
| "Firebase Authentication မှာ Email/Password ဝင်ခွင့် မဖွင့်ရသေးပါ" | Setup #1 |
| "အသုံးပြုသူအမည် (သို့) စကားဝှက် မှားနေပါတယ်" | Username = `admin` (သို့) `admin@myancast.app` · အကောင့်က **Authentication → Users** မှာ ရှိရမယ် (Firestore မဟုတ်) · Logcat မှာ tag `AdminAuth` ကြည့် |
| "ခွင့်ပြုချက် မရှိပါ — … Firestore rules ကို စစ်ပါ" | Setup #4 (rules Publish မလုပ်ရသေး / admin email မကိုက်) |
| "ခွင့်ပြုချက် မရှိပါ — … Storage rules ကို စစ်ပါ" | Setup #5 |
| "Firebase Storage ကို မဖွင့်ရသေးပါ" | Storage → Get started (Blaze လို) · သို့မဟုတ် လင့်ခ် mode သုံး |
| "MP3 ဖိုင် မဟုတ်ပါ" | `.mp3` (audio/mpeg) ပဲ လက်ခံ — m4a/wav ကို MP3 ပြောင်းပြီး တင် |
| "အသံဖိုင်ကို ဖွင့်လို့ မရပါ" | လင့်ခ်က ဖိုင်ကို တိုက်ရိုက် ဖွင့်ရမယ် (Google Drive "share" လင့်ခ် လို HTML page မဟုတ်ရ) |
| "အင်တာနက် မရှိပါ — ချိတ်ဆက်ပြီးမှ သိမ်းပါ" | Upload မစခင် စစ်တာ — လိုင်း ပြန်ရမှ သိမ်း |
