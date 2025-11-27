"""
Gomoku 极简压测 - 只测试两个 GET 接口

测试接口：
1. GET /api/ranking/leaderboard - 排行榜
2. GET /api/user/3781672231167683528 - 用户信息

使用方法：
    locust -f locustfile_minimal.py --host=https://test-api-gomoku.goodyhao.me
"""

from locust import HttpUser, task, between


class GomokuUser(HttpUser):
    """
    极简压测用户 - 只测试两个 GET 接口
    """

    # 每个任务之间等待 1-3 秒
    wait_time = between(1, 3)

    @task(5)  # 权重 5 - 更频繁
    def get_leaderboard(self):
        """获取排行榜"""
        self.client.get(
            "/api/ranking/leaderboard",
            name="GET Leaderboard"
        )

    @task(3)  # 权重 3 - 较少
    def get_user_info(self):
        """获取用户信息"""
        self.client.get(
            "/api/user/3781672231167683528",
            name="GET User Info"
        )


# ===========================
# 使用说明
# ===========================
if __name__ == "__main__":
    print("=" * 60)
    print("Gomoku 极简压测")
    print("=" * 60)
    print()
    print("测试接口：")
    print("  1. GET /api/ranking/leaderboard")
    print("  2. GET /api/user/3781672231167683528")
    print()
    print("运行方式：")
    print("  locust -f locustfile_minimal.py \\")
    print("    --host=https://test-api-gomoku.goodyhao.me")
    print()
    print("然后打开浏览器访问：http://localhost:8089")
    print("=" * 60)
