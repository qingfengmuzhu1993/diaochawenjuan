<template>
  <div class="profile" v-loading="loading">
    <div class="profile-header">
      <el-avatar :size="80" :src="profile.avatarUrl" />
      <div class="profile-info">
        <h2>{{ profile.username }}</h2>
        <p>{{ profile.bio || '这个人很懒，什么都没写...' }}</p>
        <el-tag v-if="profile.level">Lv.{{ profile.level }}</el-tag>
        <el-tag type="success">信誉 {{ profile.reputation }}</el-tag>
        <el-tag v-if="profile.verified" type="warning">已认证</el-tag>
      </div>
      <div v-if="!isSelf">
        <el-button v-if="!isFollowed" type="primary" @click="handleFollow">关注</el-button>
        <el-button v-else @click="handleUnfollow">已关注</el-button>
      </div>
    </div>
    <el-row :gutter="16" style="margin-top:24px">
      <el-col :span="8"><el-statistic title="粉丝" :value="followers.length" /></el-col>
      <el-col :span="8"><el-statistic title="关注" :value="following.length" /></el-col>
    </el-row>
    <div v-if="isSelf" style="margin-top:16px">
      <el-input v-model="editBio" placeholder="编辑个人简介" maxlength="200" />
      <el-button type="primary" size="small" @click="handleUpdate" style="margin-top:8px">保存</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userApi } from '@/api/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const auth = useAuthStore()
const profile = ref({})
const followers = ref([])
const following = ref([])
const isFollowed = ref(false)
const editBio = ref('')
const loading = ref(false)
const isSelf = computed(() => !route.params.id || route.params.id == auth.user?.id)

onMounted(async () => {
  loading.value = true
  const uid = route.params.id || auth.user?.id
  try {
    profile.value = await userApi.getProfileById(uid)
    editBio.value = profile.value.bio || ''
    followers.value = await userApi.getFollowers(uid)
    following.value = await userApi.getFollowing(uid)
    if (!isSelf.value) isFollowed.value = await userApi.isFollowing(uid)
  } catch {} finally { loading.value = false }
})

async function handleFollow() {
  try { await userApi.follow(route.params.id); isFollowed.value = true; ElMessage.success('已关注') } catch {}
}
async function handleUnfollow() {
  try { await userApi.unfollow(route.params.id); isFollowed.value = false; ElMessage.success('已取消关注') } catch {}
}
async function handleUpdate() {
  try { await userApi.updateProfile({ bio: editBio.value }); ElMessage.success('更新成功') } catch {}
}
</script>

<style scoped>
.profile-header { display: flex; align-items: center; gap: 20px; }
.profile-info { flex: 1; }
.profile-info p { color: #666; margin: 8px 0; }
.profile-info .el-tag { margin-right: 8px; }
</style>
