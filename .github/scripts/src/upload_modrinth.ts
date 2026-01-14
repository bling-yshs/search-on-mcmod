import { readdirSync } from 'node:fs'
import { join } from 'node:path'

const VERSION_MAP: Record<string, string[]> = {
  '1.21.9-1.21.10': ['1.21.9', '1.21.10'],
  '1.21-1.21.8': ['1.21', '1.21.1', '1.21.2', '1.21.3', '1.21.4', '1.21.5', '1.21.6', '1.21.7', '1.21.8'],
  '1.20.6': ['1.20.6'],
  '1.20.x': ['1.20', '1.20.1', '1.20.2', '1.20.3', '1.20.4'],
  '1.19.x': ['1.19', '1.19.1', '1.19.2', '1.19.3', '1.19.4'],
  '1.18.x': ['1.18', '1.18.1', '1.18.2'],
  '1.16.5': ['1.16.5'],
  '1.12.2': ['1.12.2'],
}

interface MatrixItem {
  name: string
  file: string
  loader: string
  mc_version: string
  game_versions: string
}

function parseFilename(filename: string): MatrixItem | null {
  const parts = filename.replace('.jar', '').split('-')
  if (parts.length >= 4 && parts[0] === 'searchonmcmod') {
    const loader = parts[1].toLowerCase()
    const mc_version = parts[2]
    const mod_version = parts[3]

    // 将loader转换为首字母大写形式，使显示更美观
    let displayLoader = loader.charAt(0).toUpperCase() + loader.slice(1)
    if (loader === 'neoforge') {
      displayLoader = 'NeoForge'
    }

    // 新的name格式：[加载器类型 游戏版本] v模组版本
    const name = `[${displayLoader} ${mc_version}] v${mod_version}`

    return {
      name,
      file: filename,
      loader,
      mc_version,
      game_versions: (VERSION_MAP[mc_version] || [mc_version]).join(','),
    }
  }
  return null
}

function main() {
  // 获取选中的版本列表
  const selectedVersions = (process.env.GITHUB_SELECTED_VERSIONS || '').split(',')

  const matrix: MatrixItem[] = []
  const releaseAssetsPath = './release_assets'

  try {
    const files = readdirSync(releaseAssetsPath)
    for (const file of files) {
      if (file.endsWith('.jar')) {
        const meta = parseFilename(file)
        if (meta) {
          // 只添加选中版本的文件
          if (selectedVersions.some(version => meta.mc_version === version)) {
            matrix.push(meta)
          }
        }
      }
    }
  }
  catch (error) {
    console.error('Error reading release_assets directory:', error)
    process.exit(1)
  }

  console.log(JSON.stringify({ include: matrix }))
}

main()
