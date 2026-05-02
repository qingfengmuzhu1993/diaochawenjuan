<template>
  <div class="marketplace">
    <div class="hero-section">
      <div class="hero-left">
        <h1 class="page-title">问卷广场</h1>
        <p class="hero-subtitle">发现高质量问卷，认真答题赚收益</p>
      </div>
      <div class="hero-actions">
        <el-button @click="showLeaderboard = true" round class="action-btn">
          <el-icon><Trophy /></el-icon>排行榜
        </el-button>
        <el-button v-if="checkedIn" round disabled class="checked-in-btn">
          <el-icon><Calendar /></el-icon>已签到
        </el-button>
        <el-button v-else type="primary" @click="handleCheckIn" round>
          <el-icon><Calendar /></el-icon>签到
        </el-button>
      </div>
    </div>

    <div class="toolbar">
      <div class="filter-pills">
        <button
          v-for="opt in sortOptions"
          :key="opt.value"
          class="filter-pill"
          :class="{ active: sort === opt.value }"
          @click="sort = opt.value; fetchList()"
        >{{ opt.label }}</button>
      </div>

      <div class="search-row">
        <el-input v-model="keyword" placeholder="搜索问卷标题..." clearable
          :prefix-icon="Search" @clear="fetchList" @keyup.enter="fetchList"
          size="default" class="search-input" />
        <el-button @click="showFilter = !showFilter" round class="filter-btn">
          <el-icon><Filter /></el-icon>{{ showFilter ? '收起筛选' : '筛选' }}
        </el-button>
      </div>
    </div>

    <transition name="slide-down">
      <div v-if="showFilter" class="filter-panel">
        <div class="filter-row">
          <span class="filter-label">奖励区间</span>
          <el-input-number v-model="minReward" :min="0" :step="0.5" size="small" placeholder="最低" controls-position="right" />
          <span class="filter-separator">—</span>
          <el-input-number v-model="maxReward" :min="0" :step="0.5" size="small" placeholder="最高" controls-position="right" />
          <span class="filter-unit">元/份</span>
        </div>
        <div class="filter-row">
          <span class="filter-label">最长时长</span>
          <el-select v-model="maxDuration" placeholder="不限" size="small" clearable class="filter-select">
            <el-option label="3 分钟内" :value="3" />
            <el-option label="5 分钟内" :value="5" />
            <el-option label="10 分钟内" :value="10" />
            <el-option label="15 分钟内" :value="15" />
          </el-select>
        </div>
        <div class="filter-actions">
          <el-button type="primary" size="small" @click="fetchList" round>应用筛选</el-button>
          <el-button size="small" @click="resetFilters" round>重置</el-button>
        </div>
      </div>
    </transition>

    <div class="card-grid">
      <div v-for="item in list" :key="item.id" class="survey-card" @click="router.push('/marketplace/' + item.id)">
        <div class="card-header">
          <h4>{{ item.title }}</h4>
          <el-tag type="warning" size="small" effect="dark" round>¥{{ item.rewardPerResponse || 0 }}/份</el-tag>
        </div>
        <p class="card-desc">{{ item.description?.substring(0, 80) || '暂无描述' }}</p>
        <div class="card-meta">
          <span class="meta-item"><el-icon><Document /></el-icon>{{ item.questionCount }} 题</span>
          <span class="meta-item"><el-icon><User /></el-icon>剩余 {{ item.remainingQuota }} 份</span>
        </div>
        <div class="card-footer">
          <div class="creator">
            <el-avatar :size="28" :style="{ background: avatarColor(item.creatorId) }">
              {{ (item.creatorName || '?')[0] }}
            </el-avatar>
            <router-link :to="'/profile/' + item.creatorId" class="creator-name" @click.stop>@{{ item.creatorName || '匿名用户' }}</router-link>
            <el-button v-if="item.creatorId && item.creatorId !== authUserId"
              link size="small" type="primary"
              @click.stop="toggleCardFollow(item.creatorId)">{{ cardFollowed[item.creatorId] ? '已关注' : '+ 关注' }}</el-button>
          </div>
          <el-button type="primary" size="small" round @click.stop="handleClaim(item)">立即回答</el-button>
        </div>
      </div>
    </div>

    <el-empty v-if="list.length === 0" description="暂无问卷，换个筛选条件试试" />

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        :page-size="20"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchList"
      />
    </div>

    <el-dialog v-model="showLeaderboard" title="🏆 排行榜" width="420px" destroy-on-close>
      <div class="filter-pills" style="justify-content:center;margin-bottom:20px">
        <button v-for="p in periods" :key="p.value"
          class="filter-pill" :class="{ active: boardPeriod === p.value }"
          @click="boardPeriod = p.value; fetchLeaderboard()"
        >{{ p.label }}</button>
      </div>
      <div v-if="leaderboard.length === 0" style="text-align:center;color:var(--app-text-muted);padding:20px">暂无排行数据</div>
      <div v-for="entry in leaderboard" :key="entry.userId" class="board-item">
        <span class="board-rank" :class="'rank-' + entry.rank">
          <template v-if="entry.rank === 1">🥇</template>
          <template v-else-if="entry.rank === 2">🥈</template>
          <template v-else-if="entry.rank === 3">🥉</template>
          <template v-else>#{{ entry.rank }}</template>
        </span>
        <el-avatar :size="40" :style="{ background: boardAvatarColor(entry.rank) }">
          {{ (entry.username || '?')[0] }}
        </el-avatar>
        <span class="board-name">{{ entry.username }}</span>
        <span class="board-earnings">¥{{ entry.earnings }}</span>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { marketplaceApi } from '@/api/marketplace'
import { userApi } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const sortOptions = [
  { label: '推荐', value: 'recommended' },
  { label: '最新', value: 'newest' },
  { label: '高奖励', value: 'highest_reward' },
  { label: '即将截止', value: 'ending_soon' },
]
const periods = [
  { label: '日榜', value: 'daily' },
  { label: '周榜', value: 'weekly' },
  { label: '月榜', value: 'monthly' },
]

const router = useRouter()
const auth = useAuthStore()
const authUserId = auth.user?.id
const list = ref([])
const cardFollowed = ref({})
const sort = ref('recommended')
const page = ref(1)
const total = ref(0)
const keyword = ref('')
const showFilter = ref(false)
const minReward = ref(null)
const maxReward = ref(null)
const maxDuration = ref(null)
const showLeaderboard = ref(false)
const boardPeriod = ref('daily')
const leaderboard = ref([])
const checkedIn = ref(false)

onMounted(() => { fetchList(); fetchMyFollowing(); fetchCheckinStatus() })

async function fetchCheckinStatus() {
  try { checkedIn.value = await marketplaceApi.getCheckinStatus() } catch {}
}

async function fetchMyFollowing() {
  if (!authUserId) return
  try {
    const users = await userApi.getFollowing(authUserId)
    users.forEach(u => { cardFollowed.value[u.id] = true })
  } catch {}
}

async function fetchList() {
  try {
    const params = { sort: sort.value, page: page.value, size: 20 }
    if (keyword.value) params.keyword = keyword.value
    if (minReward.value != null) params.minReward = minReward.value
    if (maxReward.value != null) params.maxReward = maxReward.value
    if (maxDuration.value != null) params.maxDuration = maxDuration.value
    const res = await marketplaceApi.listSurveys(params)
    list.value = res.list; total.value = res.total
  } catch {}
}

function resetFilters() {
  keyword.value = ''
  minReward.value = null; maxReward.value = null; maxDuration.value = null
  showFilter.value = false
  fetchList()
}

async function handleClaim(item) {
  try {
    const res = await marketplaceApi.claim(item.id)
    router.push('/marketplace/' + item.id + '/answer?responseId=' + res.responseId)
  } catch {}
}

async function toggleCardFollow(userId) {
  try {
    if (cardFollowed.value[userId]) {
      await userApi.unfollow(userId)
      cardFollowed.value[userId] = false
    } else {
      await userApi.follow(userId)
      cardFollowed.value[userId] = true
    }
  } catch {}
}

async function handleCheckIn() {
  try {
    const msg = await marketplaceApi.checkIn()
    checkedIn.value = true
    ElMessage.success(msg)
  } catch {}
}

async function fetchLeaderboard() {
  try { leaderboard.value = (await marketplaceApi.getLeaderboard(boardPeriod.value)).entries || [] } catch {}
}

const avatarColors = ['#0D9488', '#6366F1', '#F59E0B', '#EF4444', '#8B5CF6', '#10B981', '#EC4899', '#3B82F6']
function avatarColor(id) { return avatarColors[(id || 1) % avatarColors.length] }
function boardAvatarColor(rank) { return rank <= 3 ? ['#F59E0B','#94A3B8','#D97706'][rank-1] : avatarColors[rank % avatarColors.length] }
</script>

<style scoped>
/* ====== Hero Section ====== */
.hero-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;
}
.hero-left { flex: 1; }
.hero-subtitle {
  margin: 8px 0 0;
  font-size: 15px;
  color: var(--app-text-muted);
  font-weight: 400;
  line-height: 1.5;
}
.hero-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}
.action-btn {
  border-color: var(--app-border) !important;
  color: var(--app-text-secondary) !important;
  font-weight: 500;
}
.action-btn:hover {
  border-color: var(--app-primary) !important;
  color: var(--app-primary) !important;
}

/* ====== Toolbar ====== */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 24px;
}
.search-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.search-input {
  width: 280px;
}
.search-input :deep(.el-input__wrapper) {
  background: var(--app-bg-card);
  border-radius: 24px !important;
  padding: 6px 16px;
  box-shadow: var(--shadow-sm);
  border: 1.5px solid transparent;
  transition: all var(--transition-fast);
}
.search-input :deep(.el-input__wrapper:hover),
.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: var(--app-primary-light);
  box-shadow: 0 0 0 3px rgba(13,148,136,0.1);
}
.filter-btn {
  border-color: var(--app-border) !important;
  color: var(--app-text-secondary) !important;
  font-weight: 500;
}

/* ====== Filter Panel ====== */
.filter-panel {
  background: var(--app-bg-card);
  border-radius: var(--radius-lg);
  padding: 20px 24px;
  margin-bottom: 24px;
  border: 1px solid var(--app-border-light);
  box-shadow: var(--shadow-md);
}
.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}
.filter-label {
  width: 72px;
  font-size: 13px;
  color: var(--app-text-secondary);
  font-weight: 600;
  flex-shrink: 0;
}
.filter-separator {
  color: var(--app-text-muted);
  font-size: 13px;
}
.filter-unit {
  font-size: 12px;
  color: var(--app-text-muted);
}
.filter-actions {
  display: flex;
  gap: 8px;
  padding-top: 4px;
}
.filter-select {
  width: 160px;
}

.slide-down-enter-active { transition: all 0.25s ease-out; }
.slide-down-leave-active { transition: all 0.15s ease-in; }
.slide-down-enter-from, .slide-down-leave-to { opacity: 0; transform: translateY(-8px); }

/* ====== Card Grid ====== */
.card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}

@media (max-width: 1200px) { .card-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) { .card-grid { grid-template-columns: 1fr; } }

.survey-card {
  background: var(--app-bg-card);
  border-radius: var(--radius-lg);
  padding: 22px;
  border: 1px solid var(--app-border-light);
  box-shadow: var(--shadow-card);
  transition: all var(--transition-normal);
  cursor: pointer;
}
.survey-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-3px);
  border-color: var(--app-border);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
  gap: 8px;
}
.card-header h4 {
  margin: 0;
  font-size: 15px;
  color: var(--app-text-primary);
  font-weight: 600;
  line-height: 1.45;
  flex: 1;
  min-width: 0;
}

.card-desc {
  color: var(--app-text-muted);
  font-size: 13px;
  line-height: 1.65;
  margin: 0 0 16px;
  min-height: 42px;
}

.card-meta {
  display: flex;
  gap: 18px;
  margin-bottom: 16px;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--app-text-secondary);
  font-weight: 500;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 14px;
  border-top: 1px solid var(--app-border-light);
}

.creator {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}
.creator-name {
  color: var(--app-text-secondary);
  text-decoration: none;
  font-weight: 600;
  font-size: 13px;
}
.creator-name:hover { color: var(--app-primary); }

/* ====== Pagination ====== */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 16px 0;
}

/* ====== Leaderboard ====== */
.board-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 4px;
  border-bottom: 1px solid var(--app-border-light);
}
.board-item:last-child { border-bottom: none; }

.board-rank {
  font-weight: 700;
  font-size: 18px;
  width: 36px;
  text-align: center;
}
.rank-1, .rank-2, .rank-3 { font-size: 22px; }

.board-name {
  flex: 1;
  font-size: 15px;
  color: var(--app-text-primary);
  font-weight: 500;
}
.board-earnings {
  font-weight: 700;
  font-size: 15px;
  color: #F59E0B;
}

/* ====== Misc ====== */
.checked-in-btn {
  color: var(--app-text-muted) !important;
  border-color: var(--app-border) !important;
  background: var(--app-primary-bg) !important;
  cursor: default !important;
  font-weight: 500;
}
</style>
