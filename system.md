# Nordic Paws - Comprehensive System Architecture & Design Report

## 1. Project Overview & Purpose
**Nordic Paws** is a premium, cloud-synced pet management and wellness application built natively for Android. 
The application serves as a comprehensive central hub for pet owners. Its primary purpose is to organize pet care, reducing the cognitive load on pet owners by tracking daily routines (feeding, exercise, medication), managing detailed pet profiles, and securely synchronizing all this information across devices. It achieves this while offering a buttery-smooth, deeply engaging user interface filled with micro-animations and warm aesthetics that makes pet care organization feel seamless rather than like a chore.

---

## 2. Data Models, Schema, and Relationships
The app relies on a strongly typed, hierarchical data architecture matching NoSQL principles perfectly. Data is serialized and deserialized automatically by Firebase SDKs based on Java POJO (Plain Old Java Object) models.

### 2.1 User Model (`User.java`)
**Purpose:** Represents the human account owner.
- `uid` (String): Primary key matching Firebase Auth UID.
- `fullName` (String): Display name of the user.
- `email` (String): User's email address.
- `address` (String): Physical location for customized pet care.
- `mobileNumber` (String): Contact number.
- `createdAt` (long): Timestamp of account creation.
- `localProfileImagePath` (String): URI path to the local device storage for the avatar.
**Relationship:** A 1-to-Many relationship with Pets (stored as a subcollection under the User document).

### 2.2 Pet Model (`Pet.java`)
**Purpose:** Represents an individual pet.
- `id` (String): Auto-generated Firestore document ID.
- `name` (String): Name of the pet.
- `petCategory` (String): e.g., Dog, Cat, Bird, Horse.
- `breedAndGender` (String): e.g., "Golden Retriever • Male".
- `tagAge` (String): Human-readable age (e.g., "2 yrs 4 mos").
- `tagOther` (String): Usually reserved for allergies or microchip info.
- `stat1Val` (String): Pet's weight (e.g., "31.4 kg").
- `stat2Label` (String): e.g., "DIET".
- `stat2Val` (String): Specific diet type.
- `statusBadge` (String): General wellness notes.
- `avatarResId` (int): Fallback local drawable resource (e.g., `R.drawable.dog`).
- `localImagePath` (String): File path to the customized cached image.
**Relationship:** A 1-to-Many relationship with RoutineTasks (stored as a subcollection under the Pet document).

### 2.3 RoutineTask Model (`RoutineTask.java`)
**Purpose:** Represents a single scheduled event for a specific pet.
- `id` (String): Auto-generated Firestore ID.
- `petId` (String): Foreign key referencing the parent Pet.
- `taskName` (String): e.g., "Morning Walk".
- `time` (String): e.g., "08:00 AM".
- `category` (String): e.g., "Feeding", "Exercise", "Medication", "Grooming".
- `isCompleted` (boolean): Checkbox state.
- `frequency` (String): e.g., "DAILY", "WEEKLY".
- `dueDate` (String): Applicable for non-daily tasks.

---

## 3. Utilities & Managers

### 3.1 `FirestoreManager`
**Purpose:** A centralized singleton-like utility wrapping all Firebase Firestore calls.
- **Why it's used:** Encapsulating database logic keeps UI code clean. It prevents Fragments from directly manipulating collections, reducing errors and duplicated paths.
- **Functions:**
  - `getPetsRef()`: Generates `/users/{userId}/pets`.
  - `savePet()`, `fetchPets()`, `fetchPetById()`: Handle pet data.
  - `saveRoutineTask()`, `fetchTasksForPet()`, `deleteTask()`: Manage tasks dynamically.
  - `deletePetWithTasks()`: Implements a crucial **Cascading Delete** algorithm. Since Firestore does not auto-delete subcollections, this method fetches all child tasks, deletes them one by one, and then deletes the parent pet.

### 3.2 `LocalStorageManager`
**Purpose:** Manages device-local file storage for images.
- **Why it's used:** Cloud Storage for images costs bandwidth, increases loading times, and introduces latency. By compressing and saving avatars as Bitmaps to the app's internal `/app_images/` directory, Nordic Paws loads avatars instantly, works offline, and avoids cloud costs.
- **Functions:**
  - `saveImageToInternalStorage(Context, Uri, String)`: Decodes an image from the gallery URI, scales it down, and saves it as a high-quality JPEG.
  - `loadLocalBitmap(String)`: Instantly decodes the file path into a Bitmap for UI rendering.

---

## 4. UI Rendering, Adapters, and Views

### 4.1 Adapters
To render lists efficiently, Nordic Paws uses `RecyclerView.Adapter` implementations:
- **`PetProfileAdapter`**: Renders the grid/list of pet cards. Binds the `Pet` model to `item_pet_profile.xml`. Uses `LocalStorageManager` to parse images dynamically.
- **`RoutineTaskAdapter`**: Renders the daily timeline of tasks. Reads `isCompleted` to dynamically strike-through text and dim colors.
- **`BannerAdapter`**: Used in a `ViewPager2` on the Home screen to render sliding promotional/informational cards.
- **`DelegationTaskAdapter`**: Specifically built for the Care Delegation screen to handle assigning tasks to contacts.

### 4.2 Resource Management (`res/`)
- **`colors.xml`**: Strict palette adherence. 
  - `primary_black` (#121316) for text.
  - `secondary_yellow` (#F9E38A) and `tertiary_peach` (#FED7AA) for branding and accents.
  - `surface_white` and `bg_cream` (#F8F9FF) for non-fatiguing backgrounds.
- **`themes.xml` & `styles.xml`**: Base application theme `Theme.Material3.DayNight.NoActionBar`. Light status/navigation bars are enforced to maintain the bright, friendly aesthetic.
- **`strings.xml`**: Centralized string resources to allow for easy localization/translation in the future.
- **Icons & Images (`drawable/` & `mipmap/`)**: Over 30 custom vector XML icons (e.g., `ic_home.xml`, `paw.xml`, `meal.xml`). Vectors scale infinitely without pixelation, reducing app size compared to PNGs.

---

## 5. App Flow, Activities & Fragments

### 5.1 AndroidManifest.xml
- Controls entry points and hardware permissions.
- Grants `<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />` for avatar selection.
- Registers all activities, setting `SplashActivity` as the `MAIN` and `LAUNCHER`.

### 5.2 Onboarding & Auth Flow
1. **`SplashActivity`**: The entry point. Uses `com.airbnb.android:lottie` to parse a JSON animation (`pawsanimation`).
   - *Logic:* Checks `FirebaseAuth.getInstance().getCurrentUser()`. If logged in, routes to `MainActivity`. If not, routes to `WelcomeActivity`.
2. **`WelcomeActivity`**: Smooth landing page with static branding.
3. **`SignUpActivity` & `SignUpPasswordActivity`**: A split 2-step registration.
   - *Why 2-step?* Reduces cognitive load. Step 1 asks for identity (Name, Phone). Step 2 handles security (Password). 
   - *Logic:* Registers via Firebase, applies the `fullName` to the Auth `UserProfileChangeRequest`, and pushes the initial document to Firestore.
4. **`LoginActivity`**: Authenticates returning users.

### 5.3 MainActivity (The Shell)
- Acts as a Single-Activity Host. Contains a `BottomNavigationView` to swap fragments.
- **Top Bar Logic:** Contains a customized header. Clicking the avatar fetches the `User`'s pets from Firestore, building a dynamic `PopupMenu`. When a pet is clicked, it broadcasts the `PetSelectionListener` to all child fragments, instructing them to instantly refresh their data (e.g., the Routine view swaps to the new pet's tasks).

### 5.4 Primary Fragments
- **`HomeFragment`**: Dashboard showing pet summaries.
- **`PetProfileListFragment`**: Grid view of all pets.
- **`PetProfileDetailFragment`**: Deep dive into a single pet's stats (Age, Weight, Breed, Category).
- **`RoutineFragment`**: 
  - *Logic:* Queries `FirestoreManager` for tasks matching the current `petId`.
  - *Interactivity:* Implements `ItemTouchHelper.SimpleCallback` to detect Left/Right swipes on RecyclerView items. Swiping right opens `EditTaskBottomSheetFragment`.
- **`ProfileFragment`**: 
  - *Logic:* Fetches user info. Immediately injects `FirebaseAuth`'s `displayName` as a placeholder, eliminating blank UI screens while Firestore fetches the full profile.

### 5.5 Bottom Sheets (Seamless UX)
- *Why use Bottom Sheets?* They maintain user context. Opening an entirely new Activity to edit a single setting is jarring. Bottom Sheets slide up (`BottomSheetDialogFragment`), overlaying the screen, and slide down when done.
- **`AccountDetailsBottomSheetFragment`**: Edits the user profile, changes passwords (requiring Auth re-auth), and handles Account Deletion.
- **`AddActivityBottomSheetFragment`**: Captures new routines.
- **`EditTaskBottomSheetFragment`**: Allows inline task deletion or updates.

### 5.6 Hardware Interaction (Easter Egg)
- **`AddEditPetActivity`**: 
  - Implements Android's `SensorManager` and `SensorEventListener`.
  - *Logic:* Tracks accelerometer changes across X, Y, Z axes. If the mathematical vector exceeds a specific gravity threshold (a "Shake"), the app intercepts it, instantly clears all form fields, resets image URIs, and toasts "Form reset by shake gesture! 📳".

---

## 6. Animations & Smooth UI Transitions
Nordic Paws relies heavily on animations to feel alive:
- **Lottie (`pawsanimation.json`)**: Vector-based animation rendered natively in Splash.
- **Activity Transitions**: Whenever an Activity finishes or starts, it bypasses default Android snaps by applying custom XML animations (`overridePendingTransition(0, R.anim.slide_down)`), ensuring screens slide elegantly rather than blinking.
- **View Elevation**: Extensive use of `app:cardElevation="0dp"` combined with tinted backgrounds to create a modern, flat-but-layered UI, avoiding harsh drop-shadows.

## 7. Conclusion
Nordic Paws represents a masterclass in modern Android development. By leveraging Firebase for instant cloud-syncing, `LocalStorageManager` for zero-latency image caching, and strict Material Design / Bottom Sheet paradigms for UX, it provides a robust, fast, and emotionally engaging tool for pet care management. Every line of code, from the NoSQL schema design to the shake-to-reset hardware listener, serves to make the user's life easier.
