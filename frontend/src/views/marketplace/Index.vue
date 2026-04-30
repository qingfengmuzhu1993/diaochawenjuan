<template>
  <div class="marketplace">
    <h2>问卷广场</h2>
    <div class="filters">
      <el-radio-group v-model="sort" @change="fetchList">
        <el-radio-button value="recommended">推荐</el-radio-button>
        <el-radio-button value="newest">最新</el-radio-button>
        <el-radio-button value="highest_reward">高奖励</el-radio-button>
      </el-radio-group>
      <div class="right-stats">
        <el-button @click="showLeaderboard = true"><el-icon><Trophy /></el-icon>排行榜</el-button>
        <el-button @click="handleCheckIn"><el-icon><Calendar /></el-icon>签到</el-button>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :span="8" v-for="item in list" :key="item.id">
        <el-card shadow="hover" class="survey-card">
          <h4>{{ item.title }}</h4>
          <p class="desc">{{ item.description?.substring(0, 60) || '暂无描述' }}</p>
          <div class="meta">
            <el-tag type="warning">¥{{ item.rewardPerResponse || 0 }}/份</el-tag>
            <span>{{ item.questionCount }}题</span>
            <span>剩余{{ item.remainingQuota }}份</span>
          </div>
          <p class="creator">@{{ item.creatorName || '匿名用户' }}</p>
          <el-button type="primary" @click="handleClaim(item)">立即回答</el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-pagination v-model:current-page="page" :page-size="20" :total="total" layout="prev,pager,next" @current-change="fetchList" style="margin-top:20px;justify-content:center" />

    <!-- Leaderboard Dialog -->
    <el-dialog v-model="showLeaderboard" title="排行榜" width="400px">
      <el-radio-group v-model="boardPeriod" @change="fetchLeaderboard" style="margin-bottom:12px">
        <el-radio-button value="daily">日榜</el-radio-button>
        <el-radio-button value="weekly">周榜</el-radio-button>
        <el-radio-button value="monthly">月榜</el-radio-button>
      </el-radio-group>
      <div v-for="entry in leaderboard" :key="entry.userId" class="board-item">
        <span class="rank">#{{ entry.rank }}</span>
        <el-avatar :size="32" :src="entry.avatarUrl" />
        <span class="name">{{ entry.username }}</span>
        <span class="earnings">¥{{ entry.earnings }}</span>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { marketplaceApi } from '@/api/marketplace'
import { ElMessage } from 'element-plus'

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
.filters { display: flex; justify-content: space-between; align-items: center; margin: 16px 0; }
.survey-card { margin-bottom: 16px; cursor: pointer; }
.survey-card h4 { margin: 0 0 8px; }
.desc { color: #999; font-size: 13px; margin-bottom: 8px; }
.meta { display: flex; gap: 12px; align-items: center; margin-bottom: 8px; font-size: 13px; color: #666; }
.creator { color: #999; font-size: 12px; margin-bottom: 8px; }
.board-item { display: flex; align-items: center; gap: 10px; padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.rank { font-weight: bold; width: 30px; }
.name { flex: 1; }
.earnings { color: #e6a23c; font-weight: bold; }
</style>
