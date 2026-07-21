<template>
  <AuthenticatedLayout page-title="Notifications">
    <div class="max-w-4xl mx-auto p-6 space-y-6">
      <div class="flex justify-between items-center">
        <h2 class="text-xl font-bold text-gray-900">All Notifications</h2>
        <button @click="markAllAsRead" class="text-sm text-blue-600 hover:underline font-medium">
          Mark all as read
        </button>
      </div>

      <div v-if="!notifications || notifications.length === 0" class="text-center py-12 text-gray-500">
        No notifications yet.
      </div>

      <div v-else class="space-y-3">
        <div
          v-for="notification in notifications"
          :key="notification.id"
          :class="[
            'p-4 rounded-lg border transition',
            notification.read ? 'bg-white border-gray-200' : 'bg-blue-50 border-blue-200'
          ]"
        >
          <div class="flex justify-between items-start">
            <div class="flex-1">
              <p class="font-medium text-gray-900">{{ notification.title || 'Notification' }}</p>
              <p class="text-sm text-gray-600 mt-1">{{ notification.message }}</p>
              <p class="text-xs text-gray-400 mt-2">{{ new Date(notification.created_at).toLocaleString() }}</p>
            </div>
            <button
              v-if="!notification.read"
              @click="markAsRead(notification.id)"
              class="ml-4 text-xs text-blue-600 hover:underline"
            >
              Mark read
            </button>
          </div>
        </div>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref } from 'vue';
import { usePage, router } from '@inertiajs/vue3';
import AuthenticatedLayout from '@/Layouts/AuthenticatedLayout.vue';

const page = usePage();
const notifications = ref(page.props.notifications || []);

const markAsRead = (id) => {
  router.post(route('notifications.markAsRead', id), {}, {
    preserveScroll: true,
    onSuccess: () => {
      const note = notifications.value.find(n => n.id === id);
      if (note) note.read = true;
    }
  });
};

const markAllAsRead = () => {
  router.post(route('notifications.markAllAsRead'), {}, {
    preserveScroll: true,
    onSuccess: () => {
      notifications.value.forEach(n => n.read = true);
    }
  });
};
</script>
