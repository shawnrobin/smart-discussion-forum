# Smart Forum Desktop

A JavaFX desktop client for the Smart Discussion Forum platform. Matches the web interface design: orange brand colour, white cards, gray-50 background.

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ (with JavaFX 21) |
| Maven | 3.8+ |

> **Note:** The Laravel API server (`php artisan serve`) must be running on `http://localhost:8000` before launching the desktop app.

## Running

```bash
cd smart-forum-desktop
mvn javafx:run
```

## Building a fat JAR

```bash
mvn clean package
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -jar target/smart-forum-desktop-1.0.0.jar
```

## Project Structure

```
src/main/java/com/smartforum/
├── SmartForumApp.java          # Application entry point
├── model/                      # POJO data models (User, Group, Topic, Message, Quiz, ...)
├── service/
│   ├── ApiService.java         # OkHttp REST client (all API calls)
│   └── SessionManager.java     # Token + user session singleton
└── ui/
    ├── UiHelper.java           # Reusable component factory
    ├── LoginView.java          # Login / Register screen
    ├── MainView.java           # App shell (sidebar + header + content)
    ├── DashboardView.java      # Dashboard (stats, groups, quizzes, activity)
    ├── GroupsView.java         # Groups list
    ├── TopicsView.java         # Group topics / threads list
    ├── TopicDetailView.java    # Topic + threaded replies + reply form
    ├── QuizzesView.java        # Quizzes (active / upcoming / closed)
    ├── QuizTakeView.java       # Fullscreen quiz with countdown timer
    ├── NotificationsView.java  # All notifications with mark-as-read
    └── SettingsView.java       # Settings tabs (Profile, Account, Privacy, Danger Zone)

src/main/resources/
└── css/app.css                 # Global stylesheet matching Tailwind design system
```

## API Endpoints Used

All calls go to `http://localhost:8000/api` using `Authorization: Bearer <token>`.

| Purpose | Endpoint |
|---------|----------|
| Login | `POST /auth/login` |
| Register | `POST /auth/register` |
| Bootstrap | `GET /desktop/bootstrap` |
| My Groups | `GET /desktop/groups` |
| Topics | `GET /groups/{id}/topics` |
| Topic detail | `GET /topics/{id}` |
| Messages | `GET /topics/{id}/messages` |
| Post reply | `POST /topics/{id}/messages` |
| Mark answer | `POST /messages/{id}/mark-answer` |
| Quizzes | from bootstrap payload |
| Start attempt | `POST /quizzes/{id}/attempt` |
| Submit quiz | `POST /quiz-attempts/{id}/submit` |
| Notifications | `GET /notifications` |
| Mark read | `POST /notifications/{id}/read` |
| Logout | `POST /auth/logout` |

## Design System

The CSS in `app.css` mirrors the web Tailwind classes:

| Token | Value |
|-------|-------|
| Primary | `#ea580c` (orange-600) |
| Hover | `#c2410c` (orange-700) |
| Background | `#f9fafb` (gray-50) |
| Cards | `#ffffff` + `#e5e7eb` border |
| Danger | `#e11d48` (rose-600) |
| Success | `#16a34a` (green-600) |
| Info | `#2563eb` (blue-600) |
