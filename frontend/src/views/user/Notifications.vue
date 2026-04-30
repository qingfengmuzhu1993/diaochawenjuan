<template>
  <div class="notifications">
    <div class="header">
      <h2>通知</h2>
      <el-button @click="handleMarkAllRead">全部已读</el-button>
    </div>
    <div v-for="n in list" :key="n.id" class="notif-item" :class="{ unread: !n.isRead }" @click="handleRead(n)">
      <el-tag :type="tagType(n.type)" size="small">{{ n.type }}</el-tag>
      <span class="title">{{ n.title }}</span>
      <span class="time">{{ n.createdAt }}</span>
    </div>
    <el-empty v-if="list.length===0" description="暂无通知" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '@/api/user'

const list = ref([])

onMounted(async () => {
  try { list.value = await userApi.getNotifications(1, 50) } catch {}
})

function tagType(type) {
  const map = { system: '', interaction: 'success', transaction: 'warning', audit: 'danger' }
  return map[type] || ''
}

async function handleRead(n) {
  if (!n.isRead) {
    try { await userApi.markAsRead(n.id); n.isRead = 1 } catch {}
  }
}

async function handleMarkAllRead() {
  try { await userApi.markAllRead(); list.value.forEach(n => n.isRead = 1) } catch {}
}
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; }
.notif-item { display: flex; align-items: center; gap: 12px; padding: 12px; border-bottom: 1px solid #f0f0f0; cursor: pointer; }
.notif-item.unread { background: #f0f9ff; font-weight: bold; }
.time { color: #999; font-size: 12px; margin-left: auto; }
</style>
