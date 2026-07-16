# BWAT.md

This file provides guidance to Bwat when working with code in this repository.

## Tech Stack
- **Backend**: Laravel 13.x (PHP ^8.3)
- **Frontend**: Vue 3 + Inertia.js (server-rendered pages via Vite)
- **Styling**: Tailwind CSS 3.x with `@tailwindcss/forms` plugin
- **Icons**: `lucide-vue-next` (Lucide icon set)
- **Real-time**: Laravel Reverb + Laravel Echo + `pusher-js`
- **API Auth**: Laravel Sanctum (token-based, for mobile/third-party)
- **Charts**: Chart.js + `vue-chartjs`
- **Build**: Vite with `laravel-vite-plugin` and `@vitejs/plugin-vue`
- **Routing (JS)**: `ziggy-js` + `tightenco/ziggy` (`route()` helper available globally in Vue)

## Brand Identity
**Colors**:
- Primary accent: `#ea580c` (orange-600) — sidebar brand logo, active nav item backgrounds, lecturer/admin link icons
- Primary blue: `#2563eb` (blue-600) — guest layout gradients, primary buttons, card borders
- Page background: `#f9fafb` (gray-50) on main content; `#f8fafc` (slate-50) on admin layout
- Surface: `#ffffff` (white) cards with `#e5e7eb` (gray-200) borders and `shadow-md`
- Text: `#111827` (gray-900) headings, `#374151` (gray-700) body, `#6b7280` (gray-500) muted
- Admin sidebar: `#0f172a` (slate-900) background with `#94a3b8` (slate-400) text

**Typography**:
- Sans-serif (body & headings): `Figtree` from Tailwind config (falls back to system sans-serif)
- No monospace family configured

**Geometry**:
- Border radius: `rounded-lg` (8px) on cards, inputs, buttons; `rounded-xl` (12px) on nav items
- Spacing scale: Tailwind default
- Card padding: `p-6` (1.5rem)

**Visual language**: Clean, minimal academic platform — white card surfaces with subtle shadows, orange accent for active/interactive states, generous whitespace, rounded corners, gradient blue backgrounds on guest pages, dark sidebar on admin pages.

## Coding Conventions
- **Vue 3**: Use `<script setup>` Composition API + `defineProps`/`defineEmits` for all components. No Options API.
- **Inertia page components**: Place in `resources/js/Pages/`, one `.vue` file per route. Layouts in `resources/js/Layouts/`, shared components in `resources/js/Components/`.
- **Route helper**: The `route()` function from Ziggy is registered globally in `app.js` — use it in Vue templates and `<script setup>` for all named Laravel routes. Never hardcode URLs.
- **Icon imports**: Import from `lucide-vue-next` with destructured named imports. Rename to avoid collision with vue-router (e.g. `LogOut as LogOutIcon`).
- **Component naming**: PascalCase `.vue` files, multi-word names.
- **Laravel**: PSR-4 namespacing under `App\`. API controllers in `App\Http\Controllers\Api\`. Middleware in `App\Http\Middleware\`.
- **Migrations**: Wrap table creation in `if (!Schema::hasTable(...))` guards for idempotency.

## Architecture Notes
- **Dual routing system**: Inertia SSR pages (`routes/web.php`) serve the Vue SPA. Separate REST API (`routes/api.php`) serves mobile/third-party clients authenticated via Sanctum tokens. `bootstrap/app.php` renders JSON for all `api/*` routes automatically.
- **Role-based auth**: Four roles — `system_admin`, `group_admin`, `lecturer`, and unauthenticated default (student). Middleware aliases defined in `bootstrap/app.php`: `ensureIsAdmin` (allows system_admin, group_admin, lecturer) and `isLecturer` (allows lecturer, system_admin). Check `$request->user()->role` for finer-grained control.
- **Group discussions**: Groups contain Topics, Topics contain Messages (threaded via `parent_message_id`). Topics can be marked as `is_question` / `is_answered`. Messages support `is_answer` flag for Q&A marking.
- **Quiz system**: Quizzes belong to Groups, have Questions (with JSON `options` array + `correct_answer`), and QuizAttempts tracking scores. Has `start_time`/`end_time`/`duration_minutes`/`published` fields.
- **Moderation**: Users can have Warnings, Topics/Messages can have `flagged_status`, ModerationLog tracks admin actions, UserExclusion controls message visibility.
- **Real-time**: Broadcasting channel set up at `routes/channels.php` for `App.Models.User.{id}`. Expects Pusher-compatible driver (Laravel Reverb or Pusher).
- **Shared Inertia props**: `HandleInertiaRequests` middleware shares `auth.user` (id, name, email, username, role) and unread `notifications` (from `notifications` table with `read` boolean column).

## Commands
- `composer dev` — runs PHP dev server, queue worker, Pail log viewer, and Vite dev server concurrently (via `npx concurrently`)
- `composer setup` — full first-time setup: `composer install`, creates `.env`, generates key, runs migrations, `npm install`, `npm run build`
- `composer test` — runs `config:clear` then `php artisan test`

## Gotchas
- **Duplicate project folder**: There are two copies of the project — files at root level AND inside `smart-forum-web/` subdirectory. The root-level `composer.json`, `package.json`, `routes/`, `resources/`, `app/` appear to be the active ones. Do not edit or create files in `smart-forum-web/` unless the user explicitly confirms that's where they're working.
- **PrimaryButton hover color bug**: `PrimaryButton.vue` has `bg-blue-600` but `hover:bg-red-700` — likely a bug (hover should be a darker blue, not red). Don't propagate this pattern to new buttons.
- **NotificationBell.vue uses hardcoded mock data**, not the shared Inertia `notifications` prop from `HandleInertiaRequests`. Any new notification component should use the server-shared prop.
- **Database default is SQLite** per `.env.example` (`.env.example` lives only in `smart-forum-web/`). If switching to MySQL, set `DB_CONNECTION=mysql` and update `config/database.php` `'engine' => 'InnoDB'` to avoid key-length errors on migrations.
- **Session & queue drivers**: Both use `database` driver by default. Requires sessions table migration to have run before any page render.
- **Bootstrap 5 listed as a dep** in `package.json` alongside Tailwind. It's not imported anywhere in the current Vue components or CSS — likely vestigial. Don't assume Bootstrap classes are available.
