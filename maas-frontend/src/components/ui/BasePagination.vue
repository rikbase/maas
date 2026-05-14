<template>
  <div v-if="totalPages > 0" class="base-pagination">
    <span class="base-pagination__info">{{ total > 0 ? `${(page - 1) * pageSize + 1}-${Math.min(page * pageSize, total)} / ${total}` : '0 / 0' }}</span>
    <div class="base-pagination__controls">
      <button class="base-pagination__btn" :disabled="page <= 1" @click="go(page - 1)">&lsaquo;</button>
      <template v-for="p in visiblePages" :key="p">
        <span v-if="p === '...'" class="base-pagination__ellipsis">...</span>
        <button v-else :class="['base-pagination__btn', { 'base-pagination__btn--active': p === page }]" @click="go(p as number)">{{ p }}</button>
      </template>
      <button class="base-pagination__btn" :disabled="page >= totalPages" @click="go(page + 1)">&rsaquo;</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  page: number
  pageSize?: number
  total: number
}>(), {
  pageSize: 15,
})

const emit = defineEmits<{ 'update:page': [v: number] }>()

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))

const visiblePages = computed(() => {
  const tp = totalPages.value
  const cur = props.page
  if (tp <= 7) return Array.from({ length: tp }, (_, i) => i + 1) as number[]
  const pages: (number | string)[] = [1]
  if (cur > 3) pages.push('...')
  const start = Math.max(2, cur - 1)
  const end = Math.min(tp - 1, cur + 1)
  for (let i = start; i <= end; i++) pages.push(i)
  if (cur < tp - 2) pages.push('...')
  pages.push(tp)
  return pages
})

function go(p: number) { emit('update:page', p) }
</script>

<style scoped>
.base-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) 0;
  gap: var(--space-4);
}
.base-pagination__info {
  font-size: 0.857rem;
  color: var(--color-foreground-secondary);
}
.base-pagination__controls {
  display: flex;
  align-items: center;
  gap: 2px;
}
.base-pagination__btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  padding: 0 6px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-bg-card);
  color: var(--color-foreground);
  font-size: 0.857rem;
  cursor: pointer;
  transition: all var(--transition-fast);
}
.base-pagination__btn:hover:not(:disabled):not(.base-pagination__btn--active) {
  background: var(--color-bg-muted);
}
.base-pagination__btn--active {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}
.base-pagination__btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.base-pagination__ellipsis {
  padding: 0 4px;
  color: var(--color-muted);
}
</style>
