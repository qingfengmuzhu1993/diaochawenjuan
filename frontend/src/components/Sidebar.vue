<template>
  <aside class="sidebar">
    <div class="sidebar-brand" @click="$router.push('/marketplace')">
      <div class="brand-logo">S</div>
      <span class="brand-name">SmartSurvey</span>
    </div>

    <nav class="sidebar-nav">
      <router-link to="/marketplace" class="nav-item" active-class="nav-active">
        <el-icon><List /></el-icon>
        <span>问卷广场</span>
      </router-link>
      <router-link to="/surveys" class="nav-item" active-class="nav-active">
        <el-icon><EditPen /></el-icon>
        <span>我的问卷</span>
      </router-link>
      <router-link to="/my-answers" class="nav-item" active-class="nav-active">
        <el-icon><Document /></el-icon>
        <span>我的回答</span>
      </router-link>
      <router-link to="/notifications" class="nav-item" active-class="nav-active">
        <el-icon><Bell /></el-icon>
        <span>通知</span>
        <el-badge v-if="unreadCount > 0" :value="unreadCount" class="nav-badge" />
      </router-link>
      <router-link to="/wallet" class="nav-item" active-class="nav-active">
        <el-icon><Wallet /></el-icon>
        <span>钱包</span>
      </router-link>
      <router-link to="/profile" class="nav-item" active-class="nav-active">
        <el-icon><User /></el-icon>
        <span>个人主页</span>
      </router-link>
      <router-link v-if="auth.isAdmin" to="/admin/users" class="nav-item" active-class="nav-active">
        <el-icon><Setting /></el-icon>
        <span>管理后台</span>
      </router-link>
    </nav>

    <div class="sidebar-footer">
      <div class="user-info">
        <el-avatar :size="36" :src="auth.user?.avatarUrl" />
        <div class="user-meta">
          <span class="user-name">{{ auth.user?.username }}</span>
          <span class="user-level">Lv.{{ auth.user?.level || 1 }}</span>
        </div>
      </div>
      <el-button :icon="SwitchButton" text @click="handleLogout" title="退出登录" />
    </div>
  </aside>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userApi } from '@/api/user'
import { List, EditPen, Document, Bell, User, Setting, Wallet, SwitchButton } from '@element-plus/icons-vue'

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
.sidebar {
  width: 220px;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  background: var(--app-bg-card);
  border-right: 1px solid var(--app-border);
  display: flex;
  flex-direction: column;
  z-index: 100;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 16px;
  border-bottom: 1px solid var(--app-bg-hover);
  cursor: pointer;
}

.brand-logo {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #2DD4A8, #14B8A6);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--app-bg-card);
  font-size: 20px;
  font-weight: 700;
}

.brand-name {
  font-size: 17px;
  font-weight: 700;
  color: var(--app-text-primary);
}

.sidebar-nav {
  flex: 1;
  padding: 8px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 14px;
  color: var(--app-text-secondary);
  text-decoration: none;
  transition: all var(--transition-fast);
  margin-bottom: 2px;
  position: relative;
}

.nav-item:hover {
  background: var(--app-primary-bg);
  color: var(--app-text-primary);
}

.nav-active {
  background: var(--app-bg-hover);
  color: var(--app-primary);
  font-weight: 600;
}

.nav-badge {
  position: absolute;
  right: 12px;
}

.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--app-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-meta {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  color: var(--app-text-primary);
  font-weight: 600;
}

.user-level {
  font-size: 11px;
  color: var(--app-text-muted);
}
</style>
