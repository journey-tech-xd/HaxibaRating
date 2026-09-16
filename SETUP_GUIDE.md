# Haxiba Rating — getting a real, installable .apk

## The honest limitation first

I can't compile the `.apk` myself from inside this sandboxed workspace —
my network access here is locked down by policy and can't reach Google's
Android build servers or any backend hosting service (Firebase,
Supabase, etc.), only a couple of code-package registries. So the actual
compiling has to happen somewhere with normal internet access.

Below are two ways to get there. **Option A needs no software install at
all** — GitHub's build servers do the compiling for you, for free, and
you just download the finished file from a webpage. Option B is the
traditional way, using Android Studio on your own computer.

## What this app actually does

- Anyone with the app can add a restaurant (name + location). A Google
  Maps search link is generated automatically from that, no API key
  needed.
- Anyone can rate a restaurant on Taste, Ambience, Quality, and Service,
  each on a 0–10 scale.
- Every phone that has the app reads and writes the *same* shared
  database, so the moment your friend rates a place, your average updates
  live — no app update, no Play Store, no server for you to run.
- The detail screen shows a bar-chart breakdown of the averages per
  category plus an overall score, and a button to open the spot in Google
  Maps.

## How the data actually crosses between everyone's phones

Every copy of the app you hand out is built with the same
`google-services.json` file baked in, and that file just points the app
at one specific Firebase project (a free cloud database Google hosts for
you). So when your friend rates a place on their phone, the app writes
that rating straight to that shared project over the internet. Your
phone has a live "listener" open on the same data, so it gets pushed the
update within a second or two — no refresh button, no manual sync, no
updating the app itself. Everyone is really just reading and writing the
same single database in the cloud; the app on each phone is just the
window into it.

The only thing you need to do for this to actually work is Step 1 below
— create that one free Firebase project and drop its config file into
the project before you build. Skip that step and the app will still
build and install, it'll just have nothing to talk to.

## Step 1 — Create a free Firebase project (5 min)

Do this first, before either build option below, so the .apk you end up
with is already wired to your shared database.

1. Go to `https://console.firebase.google.com` and sign in with any
   Google account.
2. Click **Add project**, give it any name (e.g. "Haxiba Rating"), and
   finish the wizard (you can leave Google Analytics off).
3. In the project, click the **Android icon** to add an Android app.
   - Android package name: `com.journey.haxibarating` (must match
     exactly).
   - App nickname: anything, e.g. "Haxiba Rating".
   - Skip the SHA-1 field, it's not needed here.
4. Download the `google-services.json` file it offers you.
5. In this project folder, replace `app/google-services.json` (there's a
   placeholder file already there) with the one you just downloaded.
6. In the Firebase console left sidebar, go to **Build → Firestore
   Database → Create database**. Choose any region close to you, and
   start in **test mode**.
7. Once created, go to the **Rules** tab and paste in the contents of
   `firestore.rules` from this project, then click **Publish**. This
   keeps the app writable by anyone who has it installed (there's no
   login screen) while blocking edits/deletes of existing ratings.

## Option A — Get the .apk with no software install (recommended, ~5 min)

This project already includes a file at
`.github/workflows/build-apk.yml` that tells GitHub to compile the APK
for you automatically, on their servers, every time you upload code.
You never touch a terminal or install anything.

1. Go to `https://github.com`, sign in (or make a free account).
2. Click the **+** in the top right → **New repository**. Name it
   anything (e.g. `haxiba-rating`), Public or Private both work, and
   click **Create repository** — leave everything else default, don't
   add a README.
3. On the empty repo's page, click **uploading an existing file**.
4. Open this unzipped `HaxibaRating` folder on your computer, select
   *everything inside it* (all files and folders, including the hidden
   `.github` folder — if your file browser hides dotfiles/folders, enable
   "show hidden files" first), and drag them into the GitHub upload box.
5. Scroll down and click **Commit changes**.
6. Click the **Actions** tab near the top of the repo. You should see a
   run called "Build APK" already in progress (a yellow dot). If you
   don't see one starting automatically, click **Build APK** on the left,
   then **Run workflow** → **Run workflow**.
7. Wait 2–4 minutes for the yellow dot to turn into a green checkmark.
8. Click into that finished run, scroll to the bottom **Artifacts**
   section, and click **app-debug-apk** to download it. That downloads a
   small zip — unzip it and you'll have `app-debug.apk` inside: your
   real, installable file.

If the run fails (red X), click into it and open the "Build debug APK"
step to read the actual error — that's the detail worth sharing back if
you want help fixing it.

## Option B — Build it yourself in Android Studio (~15 min, one-time)

1. Install **Android Studio** (free): `https://developer.android.com/studio`.
2. Open Android Studio → **Open** → select this `HaxibaRating` folder.
3. Let it sync (first time it downloads the Android build tools, so it
   needs a normal internet connection). If sync fails, open the
   **Build** tab at the bottom and read the actual error message.
4. Once sync finishes: menu **Build → Build App Bundle(s) / APK(s) →
   Build APK(s)**.
5. When it finishes, click the **locate** link in the notification, or
   find the file yourself at:
   `app/build/outputs/apk/debug/app-debug.apk`

## Step 3 — Share it with friends, no Play Store needed

- Send `app-debug.apk` to friends however you'd send any file — WhatsApp,
  Google Drive link, AirDrop-equivalent, email attachment.
- On their phone, when they tap the file, Android will ask them to allow
  "install unknown apps" for whichever app they downloaded it through —
  that's expected for anything installed outside the Play Store, they
  just tap allow and install.
- Everyone who installs this exact file is talking to the exact same
  Firebase project, so ratings cross automatically. No updates are needed
  unless you want to change the app itself later.

## If you want to change something later

Edit the code (in Android Studio, or directly in the GitHub web editor),
then either re-upload to GitHub (Option A rebuilds automatically) or
repeat Option B, and re-share the new `app-debug.apk`. People don't need
to uninstall the old one first — installing the new file over it works,
as long as you don't change the package name.

## Good to know / limitations of this simple version

- There's no login — ratings are anonymous and anyone with the file can
  submit one. That matches "share an APK with friends" but means nothing
  stops someone from rating a place multiple times.
- The Firestore rules above are intentionally simple. If this ever grows
  beyond a friend group, you'd want real authentication (Firebase Auth,
  also free) so each person gets one vote.
- `app-debug.apk` is signed with Android's generic debug key, which is
  fine for sharing directly with friends. If you ever wanted this on the
  Play Store, you'd build a signed "release" APK/AAB instead — Android
  Studio's Build menu has a wizard for that too (Build → Generate Signed
  App Bundle / APK).
