<template>
  <div class="tunnel-hierarchy">
    <div class="card">
      <div class="header">
        <h3>隧道层级管理</h3>
        <div class="header-actions">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :show-file-list="false"
            accept=".kml"
            :on-change="handleKmlFileChange"
          >
            <el-button type="success">上传KML文件</el-button>
          </el-upload>
          <el-button type="primary" @click="handleAdd">新增管理单位</el-button>
        </div>
      </div>
      <div class="hierarchy-content" v-loading="loading">
        <el-tree
          :data="treeData"
          :props="treeProps"
          :expand-on-click-node="false"
          default-expand-all
          node-key="id"
        >
          <template #default="{ node, data }">
            <span class="custom-tree-node">
              <span class="node-label">
                <el-tag :type="getLevelType(data.level)" size="small">
                  {{ getLevelText(data.level) }}
                </el-tag>
                <span class="node-name">{{ data.tunnelName }}</span>
                <span class="node-info" v-if="data.tunnelMileage">
                  ({{ data.tunnelMileage }}km)
                </span>
              </span>
              <span class="node-actions">
                <el-button type="primary" size="small" @click="handleEdit(data)">编辑</el-button>
                <el-button type="success" size="small" @click="handleAddChild(data)" v-if="data.level < 4">添加子级</el-button>
                <el-button type="danger" size="small" @click="handleDelete(data)">删除</el-button>
              </span>
            </span>
          </template>
        </el-tree>
        <el-empty v-if="!loading && treeData.length === 0" description="暂无数据" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllTunnelTree } from '@/api/tunnel'
import { uploadKmlFile } from '@/api/tunnelParam'

const loading = ref(false)
const treeData = ref([])
const uploadRef = ref()

const treeProps = { children: 'children', label: 'tunnelName' }

const getLevelType = (level) => ({ 1: 'primary', 2: 'success', 3: 'warning', 4: 'info' }[level] || 'info')
const getLevelText = (level) => ({ 1: '管理单位', 2: '高速公路', 3: '隧道群', 4: '具体隧道' }[level] || '未知')

const getTreeData = async () => {
  loading.value = true
  try {
    const res = await getAllTunnelTree()
    if (res.code === 200) {
      treeData.value = res.data || []
    }
  } catch (error) {
    console.error('获取隧道层级失败:', error)
    ElMessage.error('获取隧道层级失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => { ElMessage.info('新增管理单位功能开发中...') }
const handleEdit = (data) => { ElMessage.info(`编辑：${data.tunnelName}`) }
const handleAddChild = (data) => { ElMessage.info(`为"${data.tunnelName}"添加子级`) }

const handleDelete = (data) => {
  ElMessageBox.confirm(`确定要删除"${data.tunnelName}"吗？`, '提示', { type: 'warning' }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// KML文件上传处理
const handleKmlFileChange = async (file) => {
  if (!file.raw.name.toLowerCase().endsWith('.kml')) {
    ElMessage.error('请选择KML文件')
    return
  }

  try {
    loading.value = true
    const res = await uploadKmlFile(file.raw)
    if (res.code === 200) {
      ElMessage.success('KML文件上传成功')
      getTreeData()
    } else {
      ElMessage.error(res.msg || 'KML文件上传失败')
    }
  } catch (error) {
    console.error('KML上传失败:', error)
    ElMessage.error('KML文件上传失败')
  } finally {
    loading.value = false
    if (uploadRef.value) {
      uploadRef.value.clearFiles()
    }
  }
}

onMounted(() => getTreeData())
</script>

<style lang="scss" scoped>
.tunnel-hierarchy {
  .header {
    display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
    .header-actions { display: flex; gap: 10px; }
  }
  .hierarchy-content {
    padding: 20px;
    .custom-tree-node {
      flex: 1; display: flex; align-items: center; justify-content: space-between; font-size: 14px; padding-right: 8px;
      .node-label { display: flex; align-items: center; gap: 8px;
        .node-name { font-weight: 500; }
        .node-info { color: #909399; font-size: 12px; }
      }
      .node-actions { display: none; gap: 4px; }
      &:hover .node-actions { display: flex; }
    }
  }
}
:deep(.el-tree-node__content) { height: 40px; &:hover { background-color: #f5f7fa; } }
</style>
