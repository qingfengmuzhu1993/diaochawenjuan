<template>
  <div class="answer-page" v-loading="loading">
    <h2>{{ survey.title }}</h2>
    <p class="desc">{{ survey.description }}</p>
    <el-progress :percentage="progress" :stroke-width="8" style="margin:16px 0" />
    <el-alert v-if="timedOut" title="答题时间已超时" type="error" show-icon :closable="false" />

    <div class="question-area" v-if="currentQuestion">
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
        <div v-else>
          <el-input v-model="currentAnswer.text" placeholder="请输入" maxlength="200" />
        </div>
      </el-card>
    </div>

    <div class="nav-buttons">
      <el-button v-if="currentIndex > 0" @click="prevQuestion">上一题</el-button>
      <el-button v-if="currentIndex < totalQuestions - 1" type="primary" @click="nextQuestion">下一题</el-button>
      <el-button v-if="currentIndex === totalQuestions - 1" type="success" :loading="submitting" @click="handleSubmit">提交问卷</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { responseApi } from '@/api/response'
import { surveyApi } from '@/api/survey'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const survey = ref({})
const questions = ref([])
const currentIndex = ref(0)
const answers = ref({})
const currentAnswer = ref({ optionIds: [], text: '', rating: 0 })
const submitting = ref(false)
const timedOut = ref(false)

const currentQuestion = computed(() => questions.value[currentIndex.value])
const totalQuestions = computed(() => questions.value.length)
const progress = computed(() => Math.round((currentIndex.value / Math.max(totalQuestions.value, 1)) * 100))
const qOptions = computed(() => {
  if (!currentQuestion.value?.options) return []
  try { return typeof currentQuestion.value.options === 'string' ? JSON.parse(currentQuestion.value.options) : currentQuestion.value.options } catch { return [] }
})

onMounted(async () => {
  try {
    const detail = await surveyApi.getDetail(route.params.id)
    survey.value = detail
    questions.value = detail.questions || []
  } catch {} finally { loading.value = false }
})

function saveCurrentAnswer() {
  const q = currentQuestion.value
  if (!q) return
  answers.value[q.id] = {
    questionId: q.id,
    answerText: currentAnswer.value.text || null,
    answerOptions: currentAnswer.value.optionIds?.length ? currentAnswer.value.optionIds : null,
    answerRating: currentAnswer.value.rating || null,
  }
}

function loadCurrentAnswer() {
  const q = currentQuestion.value
  if (!q) return
  const saved = answers.value[q.id]
  currentAnswer.value = {
    optionIds: saved?.answerOptions || [],
    text: saved?.answerText || '',
    rating: saved?.answerRating || 0,
  }
}

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

async function handleSubmit() {
  saveCurrentAnswer()
  submitting.value = true
  try {
    const startRes = await responseApi.start({ surveyId: route.params.id, channel: 'marketplace' })
    const ansList = Object.values(answers.value)
    await responseApi.submitAnswers(startRes.responseId, { answers: ansList })
    await responseApi.submit(startRes.responseId)
    ElMessage.success('提交成功！感谢您的参与')
    router.push('/marketplace')
  } catch {} finally { submitting.value = false }
}
</script>

<style scoped>
.answer-page { max-width: 700px; margin: 0 auto; }
.desc { color: #666; }
.nav-buttons { display: flex; justify-content: space-between; margin-top: 20px; }
</style>
