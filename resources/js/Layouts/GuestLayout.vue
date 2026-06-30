<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-blue-100 flex items-center justify-center px-4">
    <div class="w-full max-w-md">
      <!-- Logo/Header -->
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-blue-600">
          Smart Forum
        </h1>
        <p class="text-gray-600 mt-2">Academic Discussion Platform</p>
      </div>

      <!-- Content -->
      <div class="bg-white rounded-lg shadow-lg p-8">
        <slot></slot>
      </div>

      <!-- Footer -->
      <div class="text-center mt-6 text-sm text-gray-600">
        <p>© 2026 Smart Discussion Forum. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Additional styling if needed */
</style>