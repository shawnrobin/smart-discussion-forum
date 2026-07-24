<template>
  <div class="flex min-h-dvh items-center justify-center bg-gradient-to-br from-orange-50 via-white to-amber-100 px-4 py-8 sm:px-6">
    <div class="w-full max-w-md">
      <div class="mb-8 text-center">
        <div class="mx-auto mb-3 flex h-11 w-11 items-center justify-center rounded-xl bg-orange-600 text-xl font-black text-white shadow-sm">S</div>
        <h1 class="text-3xl font-bold text-slate-900">Smart<span class="text-orange-600">Forum</span></h1>
        <p class="mt-2 text-slate-500">Academic Discussion Platform</p>
      </div>

      <div class="rounded-2xl border border-orange-100 bg-white p-5 shadow-xl shadow-orange-100/60 sm:p-8">
        <slot />
      </div>

      <div class="mt-6 text-center text-sm text-slate-500">
        <p>© 2026 Smart Discussion Forum. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>
