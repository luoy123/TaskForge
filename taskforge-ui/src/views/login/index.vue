<template>
  <div class="login-page">
    <el-card class="login-card" shadow="hover">
      <h1>{{ title }}</h1>
      <p class="hint">Vue 3 + Vite + Element Plus 骨架 · 对接后端 /login</p>
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
        <el-button type="primary" :loading="loading" style="width: 100%" @click="onSubmit">
          登录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const title = import.meta.env.VITE_APP_TITLE
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function onSubmit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/'
    await router.replace(redirect)
  } catch {
    // 错误已在 request 拦截器提示
  } finally {
    loading.value = false
  }
}
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
</style>
