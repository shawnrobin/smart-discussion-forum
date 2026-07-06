<template>
  <div class="relative">
    <!-- Bell Button -->
    <button
      @click="toggleNotifications"
      class="relative p-2 text-gray-700 hover:bg-gray-100 rounded-lg transition"
    >
      🔔
      <span
        v-if="unreadCount > 0"
        class="absolute top-0 right-0 w-5 h-5 bg-red-600 text-white text-xs rounded-full flex items-center justify-center"
      >
        {{ unreadCount > 9 ? '9+' : unreadCount }}
      </span>
    </button>

    <!-- Notifications Dropdown -->
    <div
      v-if="showNotifications"
      class="absolute right-0 mt-2 w-80 bg-white rounded-lg shadow-lg z-50"
    >
      <div class="p-4 border-b flex justify-between items-center">
        <h3 class="font-semibold">Notifications</h3>
        <button
          @click="markAllAsRead"
          class="text-xs text-blue-600 hover:underline"
        >
          Mark all as read
        </button>
      </div>

      <div class="max-h-96 overflow-y-auto">
        <div
          v-for="notification in notifications"
          :key="notification.id"
          :class="[
            'p-4 border-b cursor-pointer hover:bg-gray-50 transition',
            !notification.read && 'bg-blue-50'
          ]"
          @click="markAsRead(notification.id)"
        >
          <div class="flex gap-3">
            <span class="text-2xl">{{ notification.icon }}</span>
            <div class="flex-1">
              <p class="text-sm font-medium text-gray-900">{{ notification.title }}</p>
              <p class="text-xs text-gray-600 mt-1">{{ notification.message }}</p>
              <p class="text-xs text-gray-500 mt-2">{{ notification.time }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="p-4 border-t text-center">
        <a href="/notifications" class="text-sm text-blue-600 hover:underline">
          View all notifications
        </a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const showNotifications = ref(false);

const notifications = ref([
  {
    id: 1,
    icon: '💬',
    title: 'New reply',
    message: 'Jane replied to "Understanding Recursion"',
    time: '5 minutes ago',
    read: false
  },
  {
    id: 2,
    icon: '@',
    title: 'Mentioned',
    message: 'Bob mentioned you in "Best Sorting Algorithm"',
    time: '1 hour ago',
    read: false
  },
  {
    id: 3,
    icon: '📝',
    title: 'Quiz published',
    message: 'Database Design quiz is now available',
    time: '2 hours ago',
    read: true
  },
  {
    id: 4,
    icon: '⚠️',
    title: 'Warning',
    message: 'Your post was flagged for review',
    time: '1 day ago',
    read: true
  }
]);

const unreadCount = computed(() => {
  return notifications.value.filter(n => !n.read).length;
});

const toggleNotifications = () => {
  showNotifications.value = !showNotifications.value;
};

const markAsRead = (id) => {
  const notification = notifications.value.find(n => n.id === id);
  if (notification) notification.read = true;
};

const markAllAsRead = () => {
  notifications.value.forEach(n => n.read = true);
};
</script>

