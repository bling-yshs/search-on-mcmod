// 这是一个示例 TypeScript 文件，用于测试 ESLint 配置

interface User {
  id: number
  name: string
  email: string
}

async function fetchUser(id: number): Promise<User> {
  console.log('Fetching user with id:', id)

  // 模拟 API 调用
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        id,
        name: 'Test User',
        email: 'test@example.com',
      })
    }, 1000)
  })
}

async function main() {
  let userId = 1 // 使用 let 而不是 const（prefer-const 已关闭）

  let user = await fetchUser(userId)
  console.log('User:', user)

  // 测试单引号和无分号的规则
  let message = 'Hello, TypeScript!'
  console.log(message)
}

main()
