<template>
  <div class="auth-page">
    <div class="auth-left">
      <div class="brand-area">
        <div class="brand-icon">S</div>
        <h1>SmartSurvey</h1>
        <p>加入我们，分享你的观点</p>
      </div>
    </div>
    <div class="auth-right">
      <div class="form-wrapper">
        <h2>创建账号</h2>
        <p class="form-subtitle">注册后即可参与问卷调研</p>
        <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleRegister">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="昵称" prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item prop="phone">
            <el-input v-model="form.phone" placeholder="手机号" prefix-icon="Phone" size="large" />
          </el-form-item>
          <el-form-item prop="smsCode">
            <el-input v-model="form.smsCode" placeholder="验证码" size="large">
              <template #append>
                <el-button :disabled="countdown > 0" @click="sendSms">
                  {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码(8-20位)" prefix-icon="Lock" show-password size="large" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" native-type="submit" :loading="loading" size="large" style="width:100%">注册</el-button>
          </el-form-item>
        </el-form>
        <p class="auth-link">已有账号？<router-link to="/login">立即登录</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api/auth'
import { ElMessage } from 'element-plus'

const auth = useAuthStore()
const router = useRouter()
const formRef = ref()
const loading = ref(false)
const countdown = ref(0)
const form = reactive({ username: '', phone: '', smsCode: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [{ required: true, pattern: /^1[3-9]\d{9}$/, message: '请输入有效手机号', trigger: 'blur' }],
  smsCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [{ required: true, min: 8, max: 20, message: '密码长度8-20位', trigger: 'blur' }],
}

async function sendSms() {
  if (countdown.value > 0) return
  if (!/^1[3-9]\d{9}$/.test(form.phone)) { ElMessage.warning('请输入有效手机号'); return }
  try {
    await authApi.sendSms(form.phone)
    ElMessage.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000)
  } catch {}
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await auth.register({ ...form })
    ElMessage.success('注册成功')
    router.push('/marketplace')
  } catch {} finally { loading.value = false }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  min-height: 100vh;
}

.auth-left {
  flex: 1;
  background: linear-gradient(135deg, #2DD4A8, #14B8A6, #0D9488);
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-area {
  text-align: center;
  color: #FFFFFF;
}

.brand-icon {
  width: 80px;
  height: 80px;
  background: rgba(255,255,255,0.2);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  font-weight: 700;
  margin: 0 auto 20px;
}

.brand-area h1 {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 8px;
}

.brand-area p {
  font-size: 16px;
  opacity: 0.85;
}

.auth-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FFFFFF;
}

.form-wrapper {
  width: 380px;
}

.form-wrapper h2 {
  font-size: 28px;
  font-weight: 700;
  color: #134E4A;
  margin: 0 0 4px;
}

.form-subtitle {
  color: #87A697;
  margin: 0 0 32px;
  font-size: 14px;
}

.auth-link {
  text-align: center;
  color: #87A697;
  font-size: 14px;
}

.auth-link a {
  color: #2DD4A8;
  font-weight: 600;
  text-decoration: none;
}
</style>
