<template>
  <div>
    <BasePageHeader :title="isEdit ? $t('mcp.servers.editTitle') : $t('mcp.servers.addTitle')" />

    <BaseSpinner v-if="loading" size="lg" />
    <BaseCard v-else>
      <form @submit.prevent="save">
        <BaseFormField :label="$t('mcp.servers.name')" required>
          <input v-model="form.name" required class="input-field" />
        </BaseFormField>
        <BaseFormField :label="$t('mcp.servers.description')">
          <textarea v-model="form.description" rows="2" class="input-field"></textarea>
        </BaseFormField>
        <BaseFormField :label="$t('mcp.servers.transport')">
          <select v-model="form.transport" class="input-field">
            <option value="stdio">stdio</option>
            <option value="sse">SSE</option>
          </select>
        </BaseFormField>
        <BaseFormField v-if="form.transport === 'stdio'" :label="$t('mcp.servers.command')">
          <input v-model="form.command" placeholder="npx -y @modelcontextprotocol/server-everything" class="input-field" />
        </BaseFormField>
        <BaseFormField v-if="form.transport === 'sse'" :label="$t('mcp.servers.url')">
          <input v-model="form.url" placeholder="https://..." class="input-field" />
        </BaseFormField>
        <BaseFormField :label="$t('mcp.servers.configJson')">
          <textarea v-model="form.configJson" rows="4" class="input-field"></textarea>
        </BaseFormField>
        <div style="margin-top: var(--space-4);">
          <BaseButton variant="primary" type="submit" :loading="saving" :disabled="saving">
            {{ saving ? '' : (isEdit ? $t('provider.update') : $t('provider.create')) }}
          </BaseButton>
        </div>
      </form>
    </BaseCard>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { mcpServerApi } from '../../api/mcp'
import { useToast } from '../../composables/useToast'
import { useI18n } from 'vue-i18n'
import BasePageHeader from '../../components/ui/BasePageHeader.vue'
import BaseCard from '../../components/ui/BaseCard.vue'
import BaseFormField from '../../components/ui/BaseFormField.vue'
import BaseButton from '../../components/ui/BaseButton.vue'
import BaseSpinner from '../../components/ui/BaseSpinner.vue'

const { t } = useI18n()
const { show } = useToast()
const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)

const loading = ref(isEdit.value)
const saving = ref(false)
const form = ref({
  name: '',
  description: '',
  transport: 'stdio',
  command: '',
  url: '',
  configJson: '{}',
})

onMounted(async () => {
  if (!isEdit.value) return
  try {
    const res = await mcpServerApi.get(route.params.id as string)
    const s = res.data
    form.value = {
      name: s.name,
      description: s.description || '',
      transport: s.transport,
      command: s.command || '',
      url: s.url || '',
      configJson: s.configJson,
    }
  } catch {
    show(t('common.error'), 'error')
    router.push('/mcp/servers')
  } finally {
    loading.value = false
  }
})

async function save() {
  saving.value = true
  try {
    if (isEdit.value) {
      await mcpServerApi.update(route.params.id as string, { ...form.value })
      show(t('mcp.servers.updated'))
    } else {
      await mcpServerApi.create({ ...form.value })
      show(t('mcp.servers.created'))
    }
    router.push('/mcp/servers')
  } catch {
    show(t('common.error'), 'error')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
form {
  max-width: 560px;
}
.input-field {
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 0.929rem;
  background: var(--color-bg-card);
  width: 100%;
  box-sizing: border-box;
}
.input-field:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
  outline: none;
}
textarea.input-field {
  font-family: var(--font-mono);
  font-size: 0.857rem;
}
</style>
