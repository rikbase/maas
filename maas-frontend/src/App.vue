<template>
  <router-view />
  <Toast :toasts="toasts" />
</template>

<script setup lang="ts">
import { useToast } from './composables/useToast'
import Toast from './components/Toast.vue'

const { toasts } = useToast()
</script>
