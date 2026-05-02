<template>
  <div class="answer-page" v-loading="loading">
    <h2>{{ survey.title }}</h2>
    <p class="desc">{{ survey.description }}</p>
    <el-progress :percentage="progress" :stroke-width="8" style="margin:16px 0" />
    <div v-if="timeLeft > 0" class="countdown-timer" :class="{ warning: timeLeft < 300 }">
      <el-icon><Clock /></el-icon>
      <span>{{ Math.floor(timeLeft/60) }}:{{ String(timeLeft%60).padStart(2,'0') }}</span>
    </div>
    <el-alert v-if="timedOut" title="答题时间已超时" type="error" show-icon :closable="false" />

    <div class="display-toggle" v-if="totalQuestions > 0">
      <el-switch v-model="conversationalMode" active-text="对话模式" inactive-text="标准模式" size="small" />
    </div>

    <div :class="conversationalMode ? 'conv-layout' : 'std-layout'">
      <transition :name="conversationalMode ? 'fade-slide' : ''" mode="out-in">
    <div :key="conversationalMode ? currentIndex : 'std'" class="question-area" v-if="currentQuestion">
      <el-card>
        <template #header>
          <span>{{ currentIndex + 1 }}. {{ currentQuestion.content }}</span>
          <el-tag v-if="currentQuestion.required===1" type="danger" size="small" style="margin-left:8px">必答</el-tag>
        </template>

        <div v-if="currentQuestion.type==='single'">
          <el-radio-group v-model="currentAnswer.optionIds">
            <el-radio v-for="opt in qOptions" :key="opt.id" :label="opt.id" style="display:block;margin:8px 0">{{ opt.text }}</el-radio>
          </el-radio-group>
        </div>
        <div v-else-if="currentQuestion.type==='multiple'">
          <el-checkbox-group v-model="currentAnswer.optionIds">
            <el-checkbox v-for="opt in qOptions" :key="opt.id" :label="opt.id" style="display:block;margin:8px 0">{{ opt.text }}</el-checkbox>
          </el-checkbox-group>
        </div>
        <div v-else-if="currentQuestion.type==='judge'">
          <el-radio-group v-model="currentAnswer.optionIds">
            <el-radio label="1" style="display:block;margin:8px 0">是</el-radio>
            <el-radio label="2" style="display:block;margin:8px 0">否</el-radio>
          </el-radio-group>
        </div>
        <div v-else-if="currentQuestion.type==='rating'">
          <el-rate v-model="currentAnswer.rating" :max="5" show-score />
        </div>
        <div v-else-if="currentQuestion.type==='essay'">
          <el-input v-model="currentAnswer.text" type="textarea" :rows="3" placeholder="请输入您的回答" maxlength="2000" show-word-limit />
        </div>
        <div v-else-if="currentQuestion.type==='fill'">
          <el-input v-model="currentAnswer.text" :placeholder="getFillPlaceholder()" maxlength="200" />
          <span style="font-size:12px;color:#999;margin-top:4px;display:inline-block">{{ getFillHint() }}</span>
        </div>
        <div v-else-if="currentQuestion.type==='matrix'">
          <table class="matrix-table">
            <thead>
              <tr>
                <th></th>
                <th v-for="col in matrixCols" :key="col.id">{{ col.text }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in matrixRows" :key="row.id">
                <td class="matrix-row-label">{{ row.text }}</td>
                <td v-for="col in matrixCols" :key="col.id" class="matrix-cell">
                  <el-radio
                    v-model="matrixAnswers[row.id]"
                    :label="col.id"
                    @change="onMatrixChange"
                  >&nbsp;</el-radio>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else-if="currentQuestion.type==='ranking'">
          <p style="font-size:13px;color:#666;margin-bottom:8px">拖拽选项排序（从上到下表示优先级从高到低）</p>
          <div class="ranking-list">
            <div
              v-for="(opt, idx) in rankingItems"
              :key="opt.id"
              class="ranking-item"
              draggable="true"
              @dragstart="onRankDragStart(idx)"
              @dragover.prevent
              @drop="onRankDrop(idx)"
            >
              <span class="ranking-num">{{ idx + 1 }}</span>
              <span>{{ opt.text }}</span>
              <el-icon style="margin-left:auto;color:#87A697"><Rank /></el-icon>
            </div>
          </div>
        </div>
        <div v-else>
          <el-input v-model="currentAnswer.text" placeholder="请输入" maxlength="200" />
        </div>
      </el-card>
    </div>
      </transition>
      <div v-if="conversationalMode && totalQuestions > 1" class="conv-dots">
        <span v-for="(q, i) in questions" :key="i"
          class="conv-dot"
          :class="{ active: i === currentIndex, done: i < currentIndex }"
          @click="goToQuestion(i)"
        />
      </div>
    </div>

    <div class="nav-buttons">
      <el-button v-if="currentIndex > 0" @click="prevQuestion">上一题</el-button>
      <el-button v-if="currentIndex < totalQuestions - 1" type="primary" @click="nextQuestion">下一题</el-button>
      <el-button v-if="currentIndex === totalQuestions - 1" type="success" :loading="submitting" @click="handleSubmit">提交问卷</el-button>
    </div>

    <div v-if="submitted" class="submit-success">
      <el-result icon="success" title="提交成功！" sub-title="审核通过后奖励将自动到账">
        <template #extra>
          <el-button type="primary" @click="handleShare">分享给好友，得额外奖励</el-button>
          <el-button @click="router.push('/marketplace')">返回广场</el-button>
        </template>
      </el-result>
      <div v-if="shareUrl" class="share-box">
        <p>好友通过你的链接完成答题，你可获得其奖励的10%</p>
        <el-input v-model="shareUrl" readonly>
          <template #append><el-button @click="copyShareUrl">复制链接</el-button></template>
        </el-input>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { responseApi } from '@/api/response'
import { surveyApi } from '@/api/survey'
import { ElMessage } from 'element-plus'
import { Rank, Clock } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const survey = ref({})
const questions = ref([])
const currentIndex = ref(0)
const answers = ref({})
const currentAnswer = ref({ optionIds: [], text: '', rating: 0 })
const submitting = ref(false)
const submitted = ref(false)
const shareUrl = ref('')
const shareSurveyId = ref(null)
const timedOut = ref(false)
const timeLeft = ref(0)
let timerInterval = null
const conversationalMode = ref(false)
let questionStartTime = 0
const matrixAnswers = ref({})
const rankingItems = ref([])
let rankDragIdx = -1

const currentQuestion = computed(() => questions.value[currentIndex.value])
const totalQuestions = computed(() => questions.value.length)
const progress = computed(() => Math.round((currentIndex.value / Math.max(totalQuestions.value, 1)) * 100))
const qOptions = computed(() => {
  if (!currentQuestion.value?.options) return []
  try { return typeof currentQuestion.value.options === 'string' ? JSON.parse(currentQuestion.value.options) : currentQuestion.value.options } catch { return [] }
})
const matrixRows = computed(() => {
  if (!currentQuestion.value?.settings) return []
  try {
    const settings = typeof currentQuestion.value.settings === 'string'
      ? JSON.parse(currentQuestion.value.settings) : currentQuestion.value.settings
    return settings.rows || []
  } catch { return [] }
})
const matrixCols = computed(() => {
  if (!currentQuestion.value?.settings) return []
  try {
    const settings = typeof currentQuestion.value.settings === 'string'
      ? JSON.parse(currentQuestion.value.settings) : currentQuestion.value.settings
    return settings.cols || []
  } catch { return [] }
})

onMounted(async () => {
  try {
    const detail = await surveyApi.getDetail(route.params.id)
    survey.value = detail
    questions.value = detail.questions || []

    const draftKey = 'answer_draft_' + route.params.id
    const draft = localStorage.getItem(draftKey)
    if (draft) {
      try {
        const parsed = JSON.parse(draft)
        if (Date.now() - parsed.savedAt < 24 * 3600 * 1000) {
          answers.value = parsed.answers || {}
          currentIndex.value = parsed.currentIndex || 0
          loadCurrentAnswer()
        }
      } catch {}
    }

    if (survey.value.timeLimitMinutes && survey.value.timeLimitMinutes > 0) {
      timeLeft.value = survey.value.timeLimitMinutes * 60
      timerInterval = setInterval(() => {
        timeLeft.value--
        if (timeLeft.value <= 0) {
          clearInterval(timerInterval)
          handleSubmit()
        }
      }, 1000)
    }
  } catch {} finally { loading.value = false }
  questionStartTime = Date.now()
})

function saveCurrentAnswer() {
  const q = currentQuestion.value
  if (!q) return
  if (q.type === 'matrix') {
    answers.value[q.id] = {
      questionId: q.id,
      answerText: null,
      answerOptions: Object.entries(matrixAnswers.value).map(([rowId, colId]) => ({ row: Number(rowId), col: colId })),
      answerRating: null,
    }
  } else if (q.type === 'ranking') {
    answers.value[q.id] = {
      questionId: q.id,
      answerText: null,
      answerOptions: rankingItems.value.map(o => o.id),
      answerRating: null,
    }
  } else {
    answers.value[q.id] = {
      questionId: q.id,
      answerText: currentAnswer.value.text || null,
      answerOptions: currentAnswer.value.optionIds?.length ? [...currentAnswer.value.optionIds] : null,
      answerRating: currentAnswer.value.rating || null,
    }
  }
  localStorage.setItem('answer_draft_' + route.params.id, JSON.stringify({
    answers: answers.value,
    currentIndex: currentIndex.value,
    savedAt: Date.now()
  }))
}

function loadCurrentAnswer() {
  const q = currentQuestion.value
  if (!q) return
  const saved = answers.value[q.id]
  if (q.type === 'matrix') {
    matrixAnswers.value = {}
    if (saved?.answerOptions) {
      const opts = typeof saved.answerOptions === 'string' ? JSON.parse(saved.answerOptions) : saved.answerOptions
      if (Array.isArray(opts)) {
        opts.forEach(o => { matrixAnswers.value[o.row] = o.col })
      }
    }
    currentAnswer.value = { optionIds: [], text: '', rating: 0 }
  } else if (q.type === 'ranking') {
    if (saved?.answerOptions?.length) {
      const optIds = saved.answerOptions
      rankingItems.value = [...qOptions.value].sort((a, b) => {
        const ai = optIds.indexOf(a.id), bi = optIds.indexOf(b.id)
        return (ai === -1 ? 999 : ai) - (bi === -1 ? 999 : bi)
      })
    } else {
      rankingItems.value = [...qOptions.value]
    }
    currentAnswer.value = { optionIds: [], text: '', rating: 0 }
  } else {
    matrixAnswers.value = {}
    rankingItems.value = []
    currentAnswer.value = {
      optionIds: saved?.answerOptions ? [...saved.answerOptions] : [],
      text: saved?.answerText || '',
      rating: saved?.answerRating || 0,
    }
  }
}

function getFillPlaceholder() {
  const q = currentQuestion.value
  if (!q?.settings) return '请输入'
  try {
    const s = typeof q.settings === 'string' ? JSON.parse(q.settings) : q.settings
    return s.placeholder || '请输入'
  } catch { return '请输入' }
}
function getFillHint() {
  const q = currentQuestion.value
  if (!q?.settings) return ''
  try {
    const s = typeof q.settings === 'string' ? JSON.parse(q.settings) : q.settings
    return s.hint || ''
  } catch { return '' }
}
function onRankDragStart(idx) { rankDragIdx = idx }
function onRankDrop(idx) {
  if (rankDragIdx === idx) return
  const item = rankingItems.value.splice(rankDragIdx, 1)[0]
  rankingItems.value.splice(idx, 0, item)
}
function onMatrixChange() { /* reactivity trigger for save */ }

function nextQuestion() {
  saveCurrentAnswer()
  if (currentIndex.value < totalQuestions.value - 1) currentIndex.value++
  loadCurrentAnswer()
}

function prevQuestion() {
  saveCurrentAnswer()
  if (currentIndex.value > 0) currentIndex.value--
  loadCurrentAnswer()
}

function goToQuestion(idx) {
  saveCurrentAnswer()
  currentIndex.value = idx
  loadCurrentAnswer()
  questionStartTime = Date.now()
}

async function handleSubmit() {
  saveCurrentAnswer()
  submitting.value = true
  try {
    let respId = route.query.responseId
    if (!respId) {
      const startRes = await responseApi.start({ surveyId: route.params.id, channel: 'marketplace' })
      respId = startRes.responseId
    }
    const ansList = Object.values(answers.value)
    await responseApi.submitAnswers(respId, { answers: ansList })
    await responseApi.submit(respId)
    submitted.value = true
    localStorage.removeItem('answer_draft_' + route.params.id)
    shareSurveyId.value = route.params.id
  } catch {} finally { submitting.value = false }
}

async function handleShare() {
  try {
    const res = await surveyApi.shareSurvey(shareSurveyId.value)
    shareUrl.value = window.location.origin + '/#/marketplace/' + res.shareUrl.split('/').pop() + '?ref=' + res.shareCode
  } catch {}
}

async function copyShareUrl() {
  try {
    await navigator.clipboard.writeText(shareUrl.value)
    ElMessage.success('链接已复制')
  } catch {
    ElMessage.warning('复制失败，请手动复制')
  }
}
</script>

<style scoped>
.answer-page { max-width: 700px; margin: 0 auto; }
.desc { color: #666; }
.nav-buttons { display: flex; justify-content: space-between; margin-top: 20px; }
.matrix-table { width: 100%; border-collapse: collapse; margin-top: 8px; }
.matrix-table th, .matrix-table td { padding: 8px 12px; border: 1px solid #E8F5EF; text-align: center; font-size: 13px; }
.matrix-table th { background: #F5FAF8; color: #134E4A; font-weight: 600; }
.matrix-row-label { text-align: left !important; font-weight: 500; color: #134E4A; }
.matrix-cell { width: 60px; }
.ranking-list { display: flex; flex-direction: column; gap: 8px; margin-top: 8px; }
.ranking-item { display: flex; align-items: center; gap: 10px; padding: 10px 14px; background: #F5FAF8; border: 1px solid #CCE4D6; border-radius: 8px; cursor: grab; transition: all 0.15s; user-select: none; }
.ranking-item:hover { background: #E8F5EF; }
.ranking-item:active { cursor: grabbing; }
.ranking-num { display: inline-flex; align-items: center; justify-content: center; width: 22px; height: 22px; border-radius: 50%; background: #0D9488; color: #fff; font-size: 12px; font-weight: 600; flex-shrink: 0; }
.submit-success { margin-top: 32px; }
.share-box { margin-top: 16px; padding: 16px; background: #F5FAF8; border-radius: 12px; }
.share-box p { font-size: 13px; color: #5F8B7A; margin: 0 0 10px; }
.countdown-timer { display: flex; align-items: center; gap: 6px; font-size: 15px; color: #0D9488; font-weight: 600; margin-bottom: 12px; }
.countdown-timer.warning { color: #F56C6C; animation: pulse 1s infinite; }
@keyframes pulse { 0%,100% { opacity: 1; } 50% { opacity: 0.5; } }
.display-toggle { display: flex; justify-content: flex-end; margin-bottom: 12px; }
.conv-layout { max-width: 500px; margin: 0 auto; }
.std-layout { max-width: 700px; margin: 0 auto; }
.fade-slide-enter-active { transition: all 0.3s ease-out; }
.fade-slide-leave-active { transition: all 0.2s ease-in; }
.fade-slide-enter-from { opacity: 0; transform: translateY(20px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-10px); }
.conv-dots { display: flex; justify-content: center; gap: 8px; margin-top: 20px; flex-wrap: wrap; }
.conv-dot { width: 10px; height: 10px; border-radius: 50%; background: #CCE4D6; cursor: pointer; transition: all 0.2s; }
.conv-dot.active { background: #0D9488; transform: scale(1.3); }
.conv-dot.done { background: #5F8B7A; }
</style>
