<template>
  <div class="base-table" :class="{ 'base-table--card': card }">
    <BaseCard v-if="card" noPadding>
      <BaseSpinner v-if="loading" centered size="lg" class="base-table__spinner" />
      <BaseEmpty v-else-if="data.length === 0" :text="emptyText" class="base-table__empty" />
      <table v-else class="base-table__table">
        <thead>
          <tr>
            <th
              v-for="col in columns"
              :key="col.key"
              :class="[`th-${col.align || 'left'}`, { 'th-sortable': col.sortable }]"
              :style="col.width ? { width: col.width } : undefined"
            >
              {{ col.label }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(row, i) in data"
            :key="rowKey ? row[rowKey] : i"
            :class="{ 'tr--clickable': !!$slots['row-click'] || !!rowClick }"
            @click="handleRowClick(row)"
          >
            <td
              v-for="col in columns"
              :key="col.key"
              :class="`td-${col.align || 'left'}`"
            >
              <slot :name="`cell-${col.key}`" :row="row" :value="row[col.key]">
                {{ row[col.key] }}
              </slot>
            </td>
          </tr>
        </tbody>
      </table>
    </BaseCard>

    <template v-else>
      <BaseSpinner v-if="loading" centered size="lg" class="base-table__spinner" />
      <BaseEmpty v-else-if="data.length === 0" :text="emptyText" class="base-table__empty" />
      <table v-else class="base-table__table">
        <thead>
          <tr>
            <th
              v-for="col in columns"
              :key="col.key"
              :class="[`th-${col.align || 'left'}`, { 'th-sortable': col.sortable }]"
              :style="col.width ? { width: col.width } : undefined"
            >
              {{ col.label }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(row, i) in data"
            :key="rowKey ? row[rowKey] : i"
            :class="{ 'tr--clickable': !!rowClick }"
            @click="handleRowClick(row)"
          >
            <td
              v-for="col in columns"
              :key="col.key"
              :class="`td-${col.align || 'left'}`"
            >
              <slot :name="`cell-${col.key}`" :row="row" :value="row[col.key]">
                {{ row[col.key] }}
              </slot>
            </td>
          </tr>
        </tbody>
      </table>
    </template>
  </div>
</template>

<script setup lang="ts">
import BaseCard from './BaseCard.vue'
import BaseSpinner from './BaseSpinner.vue'
import BaseEmpty from './BaseEmpty.vue'

export interface TableColumn {
  key: string
  label: string
  align?: 'left' | 'center' | 'right'
  width?: string
  sortable?: boolean
}

const props = withDefaults(defineProps<{
  columns: TableColumn[]
  data: any[]
  loading?: boolean
  emptyText?: string
  card?: boolean
  rowKey?: string
  rowClick?: (row: any) => void
}>(), {
  loading: false,
  card: true,
})

function handleRowClick(row: any) {
  if (props.rowClick) {
    props.rowClick(row)
  }
}
</script>

<style scoped>
.base-table__spinner {
  padding: var(--space-12);
}
.base-table__empty {
  padding: var(--space-12) 0;
}

.base-table__table {
  width: 100%;
  border-collapse: collapse;
}
.base-table__table th {
  text-align: left;
  padding: 10px 12px;
  font-weight: 600;
  font-size: 0.857rem;
  color: var(--color-foreground-secondary);
  border-bottom: 2px solid var(--color-border);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
}
.base-table__table th.th-center {
  text-align: center;
}
.base-table__table th.th-right {
  text-align: right;
}
.base-table__table td {
  padding: 10px 12px;
  border-bottom: 1px solid var(--color-border);
  font-size: 0.929rem;
  color: var(--color-foreground);
}
.base-table__table td.td-center {
  text-align: center;
}
.base-table__table td.td-right {
  text-align: right;
}
.base-table__table tbody tr:hover td {
  background: var(--color-bg-muted);
  transition: background var(--transition-fast);
}
.base-table__table tbody tr:last-child td {
  border-bottom: none;
}
.base-table__table tbody tr.tr--clickable {
  cursor: pointer;
}
</style>
