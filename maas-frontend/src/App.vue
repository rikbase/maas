<template>
  <router-view v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
  <Toast :toasts="toasts" @dismiss="dismiss" />
</template>

<script setup lang="ts">
import { useToast } from './composables/useToast'
import Toast from './components/Toast.vue'

const { toasts, dismiss } = useToast()
</script>
