<template>
  <div class="profile" v-loading="loading">
    <h1 class="page-title">个人主页</h1>
    <div class="profile-card">
      <div class="profile-header">
        <div class="avatar-wrapper" :class="{ clickable: isSelf }" @click="isSelf && triggerUpload()">
          <el-avatar :size="80" :src="profile.avatarUrl || profile.avatar_url" />
          <div v-if="isSelf" class="avatar-overlay">
            <el-icon><Camera /></el-icon>
            <span>修改</span>
          </div>
        </div>
        <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="handleAvatarUpload" />
        <div class="profile-info">
          <h2>{{ profile.username }}</h2>
          <p class="bio-line">
            {{ profile.bio || '这个人很懒，什么都没写...' }}
            <el-button v-if="isSelf" link type="primary" size="small" @click="openBioDialog">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
          </p>
          <div class="profile-tags">
            <el-tag v-if="profile.level" round>Lv.{{ profile.level }}</el-tag>
            <el-tag type="success" round>信誉 {{ profile.reputation }}</el-tag>
            <el-tag v-if="profile.verified" type="warning" round>已认证</el-tag>
          </div>
        </div>
        <div v-if="!isSelf">
          <el-button v-if="!isFollowed" type="primary" round @click="handleFollow">关注</el-button>
          <el-button v-else round @click="handleUnfollow">已关注</el-button>
        </div>
      </div>
    </div>
    <el-row :gutter="16" style="margin-top:24px">
      <el-col :span="8">
        <div class="stat-clickable" @click="openFollowDialog('followers')">
          <el-statistic title="粉丝" :value="followers.length" />
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-clickable" @click="openFollowDialog('following')">
          <el-statistic title="关注" :value="following.length" />
        </div>
      </el-col>
    </el-row>

    <!-- points history dialog -->
    <el-dialog v-model="pointsHistoryVisible" title="积分明细" width="500px" destroy-on-close>
      <div v-if="pointsHistory.length === 0" style="text-align:center;color:#999;padding:20px">暂无积分记录</div>
      <div v-for="(item, idx) in pointsHistory" :key="idx" class="points-record">
        <div class="points-record-left">
          <span class="points-record-reason">{{ item.reason }}</span>
          <span class="points-record-time">{{ formatDateTime(item.createdAt || item.created_at) }}</span>
        </div>
        <span :class="item.type === 'earn' ? 'points-earn' : 'points-spend'">
          {{ item.type === 'earn' ? '+' : '' }}{{ item.amount }}
        </span>
      </div>
    </el-dialog>

    <!-- followers/following dialog -->
    <el-dialog v-model="followDialogVisible" :title="followDialogTitle" width="420px" destroy-on-close>
      <div v-if="followDialogList.length === 0" style="text-align:center;color:#999;padding:20px">暂无数据</div>
      <div v-for="user in followDialogList" :key="user.id" class="follow-list-item">
        <router-link :to="'/profile/' + user.id" class="follow-user-link" @click="followDialogVisible = false">
          <el-avatar :size="36" :src="user.avatarUrl || user.avatar_url" />
          <div>
            <div class="follow-user-name">{{ user.username }}</div>
            <div class="follow-user-bio">{{ (user.bio || '').substring(0, 30) }}</div>
          </div>
        </router-link>
        <el-button v-if="followDialogType === 'following' && isSelf"
          link type="danger" size="small" @click="handleUnfollowInDialog(user.id)">取消关注</el-button>
      </div>
    </el-dialog>
    <el-dialog v-model="bioDialogVisible" title="编辑个人简介" width="450px" destroy-on-close>
      <el-input v-model="editBio" type="textarea" :rows="4" placeholder="介绍一下自己..." maxlength="200" show-word-limit />
      <template #footer>
        <el-button @click="bioDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate">保存</el-button>
      </template>
    </el-dialog>

    <div class="badge-section" v-if="badges.length > 0">
      <h3>勋章墙</h3>
      <div class="badge-grid">
        <div v-for="b in badges" :key="b.name" class="badge-item">
          <span class="badge-icon">{{ badgeIcon(b.icon) }}</span>
          <div>
            <div class="badge-name">{{ b.name }}</div>
            <div class="badge-desc">{{ b.description }}</div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="isSelf" class="points-section">
      <h3>积分中心</h3>
      <div class="points-balance">
        <div>
          <span class="points-num">{{ pointsBalance }}</span>
          <span class="points-label">当前积分</span>
        </div>
        <el-button link type="primary" size="small" @click="openPointsHistory">积分明细</el-button>
      </div>
      <div class="exchange-options">
        <div class="exchange-card">
          <div class="exchange-info">
            <div class="exchange-title">优先抢单权</div>
            <div class="exchange-desc">兑换后可优先抢到高奖励问卷</div>
          </div>
          <el-button type="primary" size="small" round @click="handleExchange('priority')" :loading="exchanging">
            100积分 兑换
          </el-button>
        </div>
        <div class="exchange-card">
          <div class="exchange-info">
            <div class="exchange-title">1元现金券</div>
            <div class="exchange-desc">直接抵扣提现手续费（月限5次）</div>
          </div>
          <el-button type="warning" size="small" round @click="handleExchange('cash_coupon')" :loading="exchanging">
            500积分 兑换
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userApi } from '@/api/user'
import { ElMessage } from 'element-plus'
import { Edit, Camera } from '@element-plus/icons-vue'

const route = useRoute()
const auth = useAuthStore()
const fileInput = ref(null)
const profile = ref({})
const followers = ref([])
const following = ref([])
const isFollowed = ref(false)
const editBio = ref('')
const bioDialogVisible = ref(false)
const loading = ref(false)
const badges = ref([])
const pointsBalance = ref(0)
const exchanging = ref(false)
const pointsHistoryVisible = ref(false)
const pointsHistory = ref([])
const followDialogVisible = ref(false)
const followDialogType = ref('followers')
const followDialogList = ref([])
const followDialogTitle = computed(() => followDialogType.value === 'followers' ? '粉丝' : '关注')
const isSelf = computed(() => !route.params.id || route.params.id == auth.user?.id)

async function fetchBadges() {
  try { badges.value = await userApi.getBadges() } catch {}
}

async function fetchPoints() {
  try {
    const res = await userApi.getPoints()
    pointsBalance.value = res.balance || 0
  } catch {}
}

async function handleExchange(type) {
  exchanging.value = true
  try {
    const res = await userApi.exchangePoints(type)
    ElMessage.success('兑换成功：' + res.reward)
    pointsBalance.value = parseInt(res.remaining)
  } catch {} finally { exchanging.value = false }
}

function badgeIcon(icon) {
  const map = { medal: '🏅', star: '⭐', bolt: '⚡', calendar: '📅', coin: '💰', rocket: '🚀' }
  return map[icon] || '🏆'
}

onMounted(async () => {
  loading.value = true
  const uid = route.params.id || auth.user?.id
  try {
    profile.value = await userApi.getProfileById(uid)
    editBio.value = profile.value.bio || ''
    followers.value = await userApi.getFollowers(uid)
    following.value = await userApi.getFollowing(uid)
    if (!isSelf.value) isFollowed.value = await userApi.isFollowing(uid)
    await fetchBadges()
    await fetchPoints()
  } catch {} finally { loading.value = false }
})

function openFollowDialog(type) {
  followDialogType.value = type
  followDialogList.value = type === 'followers' ? followers.value : following.value
  followDialogVisible.value = true
}
async function handleFollow() {
  try { await userApi.follow(route.params.id); isFollowed.value = true; ElMessage.success('已关注') } catch {}
}
async function handleUnfollow() {
  try { await userApi.unfollow(route.params.id); isFollowed.value = false; ElMessage.success('已取消关注') } catch {}
}
async function handleUnfollowInDialog(userId) {
  try {
    await userApi.unfollow(userId)
    // remove from the dialog list and refresh counts
    followDialogList.value = followDialogList.value.filter(u => u.id !== userId)
    following.value = following.value.filter(u => u.id !== userId)
    ElMessage.success('已取消关注')
  } catch {}
}
function triggerUpload() { fileInput.value?.click() }

async function handleAvatarUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await userApi.uploadAvatar(formData)
    profile.value.avatarUrl = res.avatarUrl
    ElMessage.success('头像更新成功')
  } catch {}
  e.target.value = ''
}

function openBioDialog() {
  editBio.value = profile.value.bio || ''
  bioDialogVisible.value = true
}
async function openPointsHistory() {
  try {
    pointsHistory.value = await userApi.getPointsHistory()
    pointsHistoryVisible.value = true
  } catch {}
}

function formatDateTime(dt) {
  if (!dt) return ''
  return dt.substring(0, 16).replace('T', ' ')
}

async function handleUpdate() {
  try {
    await userApi.updateProfile({ bio: editBio.value })
    profile.value.bio = editBio.value
    ElMessage.success('更新成功')
    bioDialogVisible.value = false
  } catch {}
}
</script>

<style scoped>
.avatar-wrapper { position: relative; border-radius: 50%; overflow: hidden; }
.avatar-wrapper.clickable { cursor: pointer; }
.avatar-overlay { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; background: rgba(0,0,0,0.45); color: #fff; font-size: 12px; opacity: 0; transition: opacity 0.2s; border-radius: 50%; }
.avatar-wrapper.clickable:hover .avatar-overlay { opacity: 1; }
.profile-card {
  background: var(--app-bg-card);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid var(--app-border-light);
  box-shadow: var(--shadow-card);
  margin-top: 20px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-info {
  flex: 1;
}

.profile-info h2 {
  margin: 0 0 4px;
  color: var(--app-text-primary);
}

.profile-info p {
  color: var(--app-text-secondary);
  margin: 0 0 12px;
}

.bio-line { display: flex; align-items: center; gap: 8px; }
.stat-clickable { cursor: pointer; border-radius: 8px; padding: 8px; transition: background 0.15s; }
.stat-clickable:hover { background: var(--app-primary-bg); }
.follow-list-item { display: flex; align-items: center; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid var(--app-border-light); }
.follow-list-item:last-child { border-bottom: none; }
.follow-user-link { display: flex; align-items: center; gap: 10px; text-decoration: none; flex: 1; }
.follow-user-name { font-size: 14px; font-weight: 500; color: var(--app-text-primary); }
.follow-user-bio { font-size: 12px; color: var(--app-text-muted); margin-top: 2px; }
.profile-tags {
  display: flex;
  gap: 8px;
}

.badge-section { margin-top: 28px; }
.badge-section h3 { color: var(--app-text-primary); font-size: 16px; margin-bottom: 12px; }
.badge-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.badge-item { display: flex; align-items: center; gap: 10px; padding: 12px; background: var(--app-primary-bg); border-radius: 10px; border: 1px solid var(--app-border-light); }
.badge-icon { font-size: 24px; flex-shrink: 0; }
.badge-name { font-size: 13px; font-weight: 600; color: var(--app-text-primary); }
.badge-desc { font-size: 11px; color: var(--app-text-muted); margin-top: 2px; }
.points-section { margin-top: 28px; }
.points-section h3 { color: var(--app-text-primary); font-size: 16px; margin-bottom: 12px; }
.points-balance { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; padding: 16px; background: linear-gradient(135deg, var(--app-primary), var(--app-primary-dark)); border-radius: 12px; }
.points-record { display: flex; align-items: center; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid var(--app-border-light); }
.points-record:last-child { border-bottom: none; }
.points-record-left { display: flex; flex-direction: column; gap: 2px; }
.points-record-reason { font-size: 14px; color: var(--app-text-primary); }
.points-record-time { font-size: 12px; color: #AAA; }
.points-earn { font-size: 15px; font-weight: 700; color: var(--app-primary); }
.points-spend { font-size: 15px; font-weight: 700; color: #F56C6C; }
.points-num { font-size: 32px; font-weight: 700; color: #fff; }
.points-label { font-size: 14px; color: rgba(255,255,255,0.8); }
.exchange-options { display: flex; gap: 12px; }
.exchange-card { flex: 1; display: flex; align-items: center; justify-content: space-between; padding: 16px; background: var(--app-primary-bg); border-radius: 12px; border: 1px solid var(--app-border-light); }
.exchange-title { font-size: 14px; font-weight: 600; color: var(--app-text-primary); }
.exchange-desc { font-size: 12px; color: var(--app-text-muted); margin-top: 4px; }
</style>
