<template>
  <div class="marketplace">
    <div class="page-header">
      <h1 class="page-title">问卷广场</h1>
      <div class="header-actions">
        <el-button @click="showLeaderboard = true" round>
          <el-icon><Trophy /></el-icon>排行榜
        </el-button>
        <el-button type="primary" @click="handleCheckIn" round>
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

    <div class="card-grid" style="margin-top:20px">
      <div v-for="item in list" :key="item.id" class="survey-card">
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
            <span>@{{ item.creatorName || '匿名用户' }}</span>
          </div>
          <el-button type="primary" size="small" round @click="handleClaim(item)">立即回答</el-button>
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
import { useRouter } from 'vue-router'
import { marketplaceApi } from '@/api/marketplace'
import { ElMessage } from 'element-plus'

const sortOptions = [
  { label: '推荐', value: 'recommended' },
  { label: '最新', value: 'newest' },
  { label: '高奖励', value: 'highest_reward' },
]
const periods = [
  { label: '日榜', value: 'daily' },
  { label: '周榜', value: 'weekly' },
  { label: '月榜', value: 'monthly' },
]

const router = useRouter()
const list = ref([])
const sort = ref('recommended')
const page = ref(1)
const total = ref(0)
const showLeaderboard = ref(false)
const boardPeriod = ref('daily')
const leaderboard = ref([])

onMounted(() => fetchList())

async function fetchList() {
  try {
    const res = await marketplaceApi.listSurveys({ sort: sort.value, page: page.value, size: 20 })
    list.value = res.list; total.value = res.total
  } catch {}
}

async function handleClaim(item) {
  try {
    const res = await marketplaceApi.claim(item.id)
    router.push('/marketplace/' + item.id + '?responseId=' + res.responseId)
  } catch {}
}

async function handleCheckIn() {
  try {
    const msg = await marketplaceApi.checkIn()
    ElMessage.success(msg)
  } catch {}
}

async function fetchLeaderboard() {
  try { leaderboard.value = (await marketplaceApi.getLeaderboard(boardPeriod.value)).entries || [] } catch {}
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  color: #134E4A;
  font-weight: 700;
}

.filter-pills {
  display: flex;
  gap: 8px;
}

.filter-pill {
  padding: 6px 18px;
  border-radius: 20px;
  border: 1px solid #CCE4D6;
  background: #F5FAF8;
  color: #5F8B7A;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-pill:hover {
  background: #E8F5EF;
  border-color: #87A697;
}

.filter-pill.active {
  background: #0D9488;
  border-color: #0D9488;
  color: #FFFFFF;
}

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

.header-actions {
  display: flex;
  gap: 8px;
}

.survey-card {
  background: #FFFFFF;
  border-radius: 16px;
  padding: 20px;
  border: 1px solid #E8F5EF;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  transition: all 0.2s;
  cursor: pointer;
}

.survey-card:hover {
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
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
  color: #134E4A;
  font-weight: 600;
  line-height: 1.4;
}

.card-desc {
  color: #87A697;
  font-size: 13px;
  line-height: 1.6;
  margin: 0 0 14px;
}

.card-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 14px;
  font-size: 13px;
  color: #5F8B7A;
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
  color: #87A697;
}

.board-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #E8F5EF;
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
  color: #134E4A;
}

.board-earnings {
  font-weight: 700;
  color: #F59E0B;
}
</style>
