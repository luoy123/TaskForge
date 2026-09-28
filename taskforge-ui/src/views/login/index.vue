<template>
  <div class="login-page">
    <el-card class="login-card" shadow="hover">
      <h1>{{ title }}</h1>
      <p class="hint">Vue 3 + Vite + Element Plus · 对接 /login · Sprint 06 验证码</p>
      <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="onSubmit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            show-password
            prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item v-if="captchaEnabled" prop="code">
          <div class="captcha-row">
            <el-input v-model="form.code" placeholder="验证码" maxlength="8" />
            <img
              v-if="captchaImg"
              class="captcha-img"
              :src="captchaImg"
              alt="验证码"
              title="点击刷新"
              @click="loadCaptcha"
            />
            <el-button v-else link type="primary" @click="loadCaptcha">刷新</el-button>
          </div>
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width: 100%" @click="onSubmit">
          登录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptchaImage } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const title = import.meta.env.VITE_APP_TITLE
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const captchaEnabled = ref(false)
const captchaUuid = ref('')
const captchaRawImg = ref('')

const form = reactive({
  username: 'admin',
  password: '',
  code: '',
})

const captchaImg = computed(() => {
  const raw = captchaRawImg.value
  if (!raw) return ''
  if (raw.startsWith('data:')) return raw
  return `data:image/png;base64,${raw}`
})

const rules = computed(() => {
  const base = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  }
  if (captchaEnabled.value) {
    base.code = [{ required: true, message: '请输入验证码', trigger: 'blur' }]
  }
  return base
})

async function loadCaptcha() {
  try {
    const { data: res } = await getCaptchaImage()
    const data = res.data || {}
    captchaEnabled.value = !!data.captchaEnabled
    captchaUuid.value = data.uuid || ''
    captchaRawImg.value = data.img || ''
    form.code = ''
  } catch {
    captchaEnabled.value = false
    captchaUuid.value = ''
    captchaRawImg.value = ''
  }
}

async function onSubmit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    const payload = {
      username: form.username,
      password: form.password,
    }
    if (captchaEnabled.value) {
      payload.code = form.code
      payload.uuid = captchaUuid.value
    }
    await userStore.login(payload)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/'
    await router.replace(redirect)
  } catch {
    // 错误已在 request 拦截器提示；失败后刷新验证码
    if (captchaEnabled.value) {
      await loadCaptcha()
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCaptcha()
})
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: linear-gradient(145deg, #0f172a 0%, #1e3a5f 50%, #0f766e 100%);
}

.login-card {
  width: min(400px, 92vw);
  border: none;

  h1 {
    margin: 0 0 8px;
    font-size: 28px;
    letter-spacing: 0.04em;
  }

  .hint {
    margin: 0 0 24px;
    color: #64748b;
    font-size: 13px;
  }
}

.captcha-row {
  display: flex;
  align-items: stretch;
  gap: 10px;
  width: 100%;

  .el-input {
    flex: 1;
  }
}

.captcha-img {
  height: 32px;
  width: 96px;
  border-radius: var(--el-border-radius-base, 4px);
  border: 1px solid var(--el-border-color);
  cursor: pointer;
  object-fit: contain;
  background: #fff;
  flex-shrink: 0;
  box-sizing: border-box;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;

  &:hover {
    border-color: var(--el-color-primary);
    box-shadow: 0 0 0 1px var(--el-color-primary-light-7);
  }
}
</style>
