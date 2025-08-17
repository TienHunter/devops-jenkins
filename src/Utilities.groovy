import java.io.Serializable

class Utilities implements Serializable {
	def steps
	Utilities(steps) {
		this.steps = steps
	}

	def mergeCode(String fromBranch, String toBranch) {
		steps.echo "[Utilities] Merge ${fromBranch} vào ${toBranch}"
		def username = steps.env.GIT_USERNAME
		def password = steps.env.GIT_PASSWORD
		steps.sh """
			git config user.email 'tienkbtnhp@gmail.com'
			git config user.name 'TienHunter'
			# Tạo nhánh nếu chưa tồn tại
			if ! git show-ref --verify --quiet refs/heads/${toBranch}; then
				git checkout -b ${toBranch}
			else
				git checkout ${toBranch}
			fi
			git merge origin/${fromBranch}
			git remote set-url origin https://${username}:${password}@github.com/TienHunter/workfms-fe.git
			git push origin ${toBranch}
		"""
	}
}

// Ví dụ sử dụng class Utilities trong pipeline script:
// def utils = new Utilities(this)
// utils.mergeCode('develop', 'main')
