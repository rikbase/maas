<template>
  <div>
    <BasePageHeader :title="$t('key.createTitle')">
      <template #actions>
        <BaseButton variant="secondary" size="sm" @click="$router.push('/keys')">
          {{ $t('common.cancel') }}
        </BaseButton>
      </template>
    </BasePageHeader>

    <BaseCard>
      <form v-if="!createdKey" @submit.prevent="save" class="key-form">
        <BaseFormField :label="$t('key.name')" required>
          <input
            v-model="form.name"
            class="form-input"
            :placeholder="$t('key.name')"
            required
            :disabled="saving"
          />
        </BaseFormField>

        <BaseFormField :label="$t('key.type')" required>
          <select v-model="form.keyType" class="form-input" required :disabled="saving">
            <option value="application">{{ $t('key.types.application') }}</option>
            <option value="team">{{ $t('key.types.team') }}</option>
            <option value="root">{{ $t('key.types.root') }}</option>
          </select>
        </BaseFormField>

        <div class="form-actions">
          <BaseButton type="submit" variant="primary" :loading="saving" :disabled="saving">
            {{ saving ? $t('common.saving') : $t('key.create') }}
          </BaseButton>
          <BaseButton variant="secondary" @click="$router.push('/keys')">
            {{ $t('common.cancel') }}
          </BaseButton>
        </div>
      </form>

      <!-- Key created result -->
      <div v-else class="key-result">
        <div class="key-result__header">
          <BaseBadge variant="warning">{{ $t('key.keyCreated') }}</BaseBadge>
        </div>
        <p class="key-result__warning">{{ $t('key.copyWarning') }}</p>
        <div class="key-result__box">
          <code class="key-result__value">{{ createdKey }}</code>
          <button class="key-result__copy" @click="copyKey" :title="$t('common.copy')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
          </button>
        </div>
        <div class="form-actions">
          <BaseButton variant="primary" @click="$router.push('/keys')">
            {{ $t('common.confirm') }}
          </BaseButton>
        </div>
      </div>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { keyApi } from '../../api/keys'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseCard from '../../components/ui/BaseCard.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseFormField from '../../components/ui/BaseFormField.vue'
import BaseBadge from '../../components/ui/BaseBadge.vue'

const { t } = useI18n()
const { show } = useToast()

const form = ref({ name: '', keyType: 'application' as const })
const createdKey = ref<string | null>(null)
const saving = ref(false)

async function save() {
  saving.value = true
  try {
    const res = await keyApi.create({ ...form.value })
    createdKey.value = res.data.rawKey || ''
    show(t('key.created'))
  } catch {
    show(t('common.error'), 'error')
  } finally {
    saving.value = false
  }
}

async function copyKey() {
  if (!createdKey.value) return
  try {
    await navigator.clipboard.writeText(createdKey.value)
    show(t('common.copied'))
  } catch {
    show(t('common.error'), 'error')
  }
}
</script>

<style scoped>
.key-form {
  max-width: 480px;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 0.929rem;
  color: var(--color-foreground);
  background: var(--color-bg-card);
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
  box-sizing: border-box;
}

.form-input:focus {
  border-color: var(--color-primary);
  outline: none;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
}

select.form-input {
  cursor: pointer;
}

.form-actions {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-6);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}

/* Key result display */
.key-result {
  max-width: 600px;
}

.key-result__header {
  margin-bottom: var(--space-2);
}

.key-result__warning {
  color: var(--color-foreground-secondary);
  font-size: 0.929rem;
  margin: 0 0 var(--space-4);
  line-height: 1.5;
}

.key-result__box {
  display: flex;
  align-items: flex-start;
  gap: var(--space-2);
  background: var(--color-bg-page);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-3);
  margin-bottom: var(--space-4);
}

.key-result__value {
  flex: 1;
  font-family: var(--font-mono);
  font-size: 0.857rem;
  word-break: break-all;
  color: var(--color-foreground);
  line-height: 1.5;
}

.key-result__copy {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-bg-card);
  color: var(--color-foreground-secondary);
  cursor: pointer;
  flex-shrink: 0;
  transition: all var(--transition-fast);
}

.key-result__copy:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
</style>
