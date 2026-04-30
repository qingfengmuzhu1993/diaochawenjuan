<template>
  <el-menu mode="horizontal" :ellipsis="false" class="navbar" router>
    <el-menu-item index="/marketplace">
      <el-icon><List /></el-icon>
      <span>问卷广场</span>
    </el-menu-item>
    <el-menu-item index="/surveys">
      <el-icon><EditPen /></el-icon>
      <span>我的问卷</span>
    </el-menu-item>
    <div class="flex-grow" />
    <el-menu-item v-if="auth.isAdmin" index="/admin/users">
      <el-icon><Setting /></el-icon>
      <span>管理后台</span>
    </el-menu-item>
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="nav-badge">
      <el-menu-item index="/notifications">
        <el-icon><Bell /></el-icon>
      </el-menu-item>
    </el-badge>
    <el-sub-menu>
      <template #title>
        <el-avatar :size="28" :src="auth.user?.avatarUrl" />
        <span>{{ auth.user?.username }}</span>
      </template>
      <el-menu-item index="/profile">个人主页</el-menu-item>
      <el-menu-item index="/wallet">钱包</el-menu-item>
      <el-menu-item @click="handleLogout">退出登录</el-menu-item>
    </el-sub-menu>
  </el-menu>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userApi } from '@/api/user'

const auth = useAuthStore()
const router = useRouter()
const unreadCount = ref(0)

onMounted(async () => {
  try { unreadCount.value = await userApi.getUnreadCount() } catch {}
})

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.navbar { padding: 0 20px; }
.flex-grow { flex-grow: 1; }
.nav-badge { margin-top: 8px; }
</style>
