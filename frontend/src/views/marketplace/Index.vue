<template>
  <div class="marketplace">
    <div class="page-header">
      <h1 class="page-title">问卷广场</h1>
      <div class="header-actions">
        <el-button @click="showLeaderboard = true" round>
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

    <div class="filter-pills">
      <button
        v-for="opt in sortOptions"
        :key="opt.value"
        class="filter-pill"
        :class="{ active: sort === opt.value }"
        @click="sort = opt.value; fetchList()"
      >{{ opt.label }}</button>
    </div>

    <div style="display:flex;gap:12px;align-items:center;margin-bottom:8px">
      <el-input v-model="keyword" placeholder="搜索问卷标题..." clearable
        :prefix-icon="Search" @clear="fetchList" @keyup.enter="fetchList"
        style="max-width:320px" size="default" />
      <el-button @click="showFilter = !showFilter" round>
        <el-icon><Filter /></el-icon>筛选
      </el-button>
    </div>

    <div v-if="showFilter" class="filter-panel">
      <div class="filter-row">
        <span class="filter-label">奖励区间</span>
        <el-input-number v-model="minReward" :min="0" :step="0.5" size="small" placeholder="最低" controls-position="right" style="width:120px" />
        <span style="margin:0 8px;color:#999">—</span>
        <el-input-number v-model="maxReward" :min="0" :step="0.5" size="small" placeholder="最高" controls-position="right" style="width:120px" />
      </div>
      <div class="filter-row">
        <span class="filter-label">最长时长</span>
        <el-select v-model="maxDuration" placeholder="不限" size="small" clearable style="width:160px">
          <el-option label="3分钟内" :value="3" />
          <el-option label="5分钟内" :value="5" />
          <el-option label="10分钟内" :value="10" />
          <el-option label="15分钟内" :value="15" />
        </el-select>
      </div>
      <div class="filter-row">
        <el-button type="primary" size="small" @click="fetchList">应用筛选</el-button>
        <el-button size="small" @click="resetFilters">重置</el-button>
      </div>
    </div>

    <div class="card-grid" style="margin-top:20px">
      <div v-for="item in list" :key="item.id" class="survey-card" @click="router.push('/marketplace/' + item.id)">
        <div class="card-header">
          <h4>{{ item.title }}</h4>
          <el-tag type="warning" size="small" effect="plain" round>¥{{ item.rewardPerResponse || 0 }}/份</el-tag>
        </div>
        <p class="card-desc">{{ item.description?.substring(0, 80) || '暂无描述' }}</p>
        <div class="card-meta">
          <span><el-icon><Document /></el-icon>{{ item.questionCount }}题</span>
          <span><el-icon><User /></el-icon>剩余{{ item.remainingQuota }}份</span>
        </div>
        <div class="card-footer">
          <div class="creator">
            <el-avatar :size="24" />
            <router-link :to="'/profile/' + item.creatorId" class="creator-name" @click.stop>@{{ item.creatorName || '匿名用户' }}</router-link>
            <el-button v-if="item.creatorId && item.creatorId !== authUserId"
              link size="small" type="primary"
              @click.stop="toggleCardFollow(item.creatorId)">{{ cardFollowed[item.creatorId] ? '已关注' : '+ 关注' }}</el-button>
          </div>
          <el-button type="primary" size="small" round @click.stop="handleClaim(item)">立即回答</el-button>
        </div>
      </div>
    </div>

    <el-empty v-if="list.length === 0" description="暂无问卷" />

    <div style="display:flex;justify-content:center;margin-top:24px">
      <el-pagination
        v-model:current-page="page"
        :page-size="20"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchList"
      />
    </div>

    <el-dialog v-model="showLeaderboard" title="排行榜" width="420px" destroy-on-close>
      <div class="filter-pills" style="margin-bottom:16px">
        <button
          v-for="p in periods"
          :key="p.value"
          class="filter-pill"
          :class="{ active: boardPeriod === p.value }"
          @click="boardPeriod = p.value; fetchLeaderboard()"
        >{{ p.label }}</button>
      </div>
      <div v-for="entry in leaderboard" :key="entry.userId" class="board-item">
        <span class="board-rank" :class="'rank-' + entry.rank">#{{ entry.rank }}</span>
        <el-avatar :size="36" :src="entry.avatarUrl" />
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
</script>

<style scoped>
.card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

@media (max-width: 1200px) {
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .card-grid {
    grid-template-columns: 1fr;
  }
}

.survey-card {
  background: var(--app-bg-card);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid var(--app-border-light);
  box-shadow: var(--shadow-card);
  transition: all var(--transition-normal);
  cursor: pointer;
}

.survey-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.card-header h4 {
  margin: 0;
  font-size: 15px;
  color: var(--app-text-primary);
  font-weight: 600;
  line-height: 1.4;
}

.card-desc {
  color: var(--app-text-muted);
  font-size: 13px;
  line-height: 1.6;
  margin: 0 0 14px;
}

.card-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 14px;
  font-size: 13px;
  color: var(--app-text-secondary);
}

.card-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.creator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--app-text-muted);
}
.creator-name { color: var(--app-text-secondary); text-decoration: none; font-weight: 500; }
.creator-name:hover { color: var(--app-primary); text-decoration: underline; }

.board-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--app-border-light);
}

.board-item:last-child {
  border-bottom: none;
}

.board-rank {
  font-weight: 700;
  font-size: 16px;
  width: 32px;
}

.rank-1 { color: #F59E0B; }
.rank-2 { color: #87A697; }
.rank-3 { color: #D97706; }

.board-name {
  flex: 1;
  font-size: 14px;
  color: var(--app-text-primary);
}

.board-earnings {
  font-weight: 700;
  color: #F59E0B;
}
.checked-in-btn { color: var(--app-text-muted) !important; border-color: #CCE4D6 !important; background: var(--app-primary-bg) !important; cursor: default !important; }

.filter-panel { background: var(--app-primary-bg); border-radius: 12px; padding: 16px; margin-bottom: 16px; }
.filter-row { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.filter-row:last-child { margin-bottom: 0; }
.filter-label { width: 70px; font-size: 13px; color: var(--app-text-secondary); font-weight: 500; flex-shrink: 0; }
</style>
