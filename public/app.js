const App = {
    token: localStorage.getItem('token'),
    isAdmin: localStorage.getItem('isAdmin') === 'true',
    username: localStorage.getItem('username'),
    currentQuiz: null,
    timer: null,
    timeLeft: 30,

    async init() {
        if(this.token) {
            this.isAdmin ? this.renderAdmin() : this.renderStudent();
        } else {
            this.renderAuth();
        }
    },

    async fetchApi(url, options = {}) {
        const headers = { 'Content-Type': 'application/json' };
        if(this.token) Object.assign(headers, { 'Authorization': 'Bearer ' + this.token });
        
        const res = await fetch(url, { ...options, headers });
        if(res.status === 401) {
            this.logout();
            throw new Error("Unauthorized");
        }
        return res.json();
    },

    toast(msg) {
        const container = document.getElementById('toast-container');
        const d = document.createElement('div');
        d.className = 'toast';
        d.textContent = msg;
        container.appendChild(d);
        setTimeout(() => d.remove(), 3000);
    },

    logout() {
        this.token = null; this.isAdmin = false; this.username = null;
        localStorage.clear();
        this.renderAuth();
    },

    // ==========================================
    // RENDER: Auth
    // ==========================================
    renderAuth() {
        const html = `
        <div class="glass-card auth-container fade-in">
            <h1>Welcome Back</h1>
            <p>Sign in to your Quiz System account</p>
            <form id="authForm" onsubmit="event.preventDefault(); App.login();">
                <div class="input-group">
                    <label>Username</label>
                    <input type="text" id="username" required>
                </div>
                <div class="input-group">
                    <label>Password</label>
                    <input type="password" id="password" required>
                </div>
                <button type="submit" class="btn btn-primary">Login</button>
                <button type="button" class="btn btn-secondary" onclick="App.register()">Create Account</button>
            </form>
        </div>`;
        document.getElementById('app').innerHTML = html;
    },

    async login() {
        const {username, password} = this.getAuthFields();
        try {
            const data = await this.fetchApi('/api/login', { method: 'POST', body: JSON.stringify({username, password})});
            if(data.success) {
                this.token = data.token; this.isAdmin = data.isAdmin; this.username = data.username;
                localStorage.setItem('token', this.token);
                localStorage.setItem('isAdmin', this.isAdmin);
                localStorage.setItem('username', this.username);
                this.toast("Logged in successfully");
                this.init();
            } else {
                this.toast(data.message);
            }
        } catch(e) { console.error(e); }
    },

    async register() {
        const {username, password} = this.getAuthFields();
        if(!username || password.length < 6) return this.toast("Valid username and password (6+ chars) required");
        try {
            const data = await this.fetchApi('/api/register', { method: 'POST', body: JSON.stringify({username, password})});
            if(data.success) this.toast("Account created! Please login.");
            else this.toast(data.message);
        } catch(e) { console.error(e); }
    },

    getAuthFields() {
        return { username: document.getElementById('username').value, password: document.getElementById('password').value };
    },

    // ==========================================
    // RENDER: Admin Dashboard
    // ==========================================
    async renderAdmin() {
        document.getElementById('app').innerHTML = `
        <div class="glass-card fade-in" style="max-width: 1000px; margin:auto;">
            <div class="nav-bar">
                <h2>Admin Dashboard</h2>
                <button class="btn btn-secondary" style="width: auto; margin:0" onclick="App.logout()">Logout</button>
            </div>
            <div class="dashboard-grid">
                <div>
                    <h3>Quick Stats</h3>
                    <div class="glass-card" style="margin-top:1rem; padding:1.5rem" id="adminStats">Loading...</div>
                </div>
                <div>
                    <h3>Recent Results</h3>
                    <div style="overflow-x:auto;">
                        <table id="resultsTable"><tr><th>User</th><th>Grade</th><th>Score</th></tr></table>
                    </div>
                </div>
            </div>
            
            <h3 style="margin-top:3rem; margin-bottom:1rem">Add New Question</h3>
            <div class="glass-card form-grid" style="padding: 1.5rem;">
                <div class="input-group"><label>QuestionText</label><input type="text" id="qText"></div>
                ${['A','B','C','D'].map(l => `<div class="input-group"><label>Option ${l}</label><input type="text" id="qOpt${l}"></div>`).join('')}
                <div class="input-group"><label>Correct Option</label><select id="qCorrect"><option value="0">A</option><option value="1">B</option><option value="2">C</option><option value="3">D</option></select></div>
                <div class="input-group"><label>Category</label><input type="text" id="qCat" value="General"></div>
                <div class="input-group"><label>Difficulty</label><select id="qDiff"><option value="1">Easy</option><option value="2">Medium</option><option value="3">Hard</option></select></div>
                <button class="btn btn-primary" onclick="App.addQuestion()">Save Question</button>
            </div>
        </div>`;
        this.loadAdminData();
    },

    async loadAdminData() {
        const rRes = await this.fetchApi('/api/results');
        const table = document.getElementById('resultsTable');
        rRes.slice(0, 5).forEach(r => {
            const tr = document.createElement('tr');
            tr.innerHTML = `<td>${r.username}</td><td><span class="grade-pill ${r.grade.includes('F') ? 'grade-bad' : 'grade-good'}">${r.grade}</span></td><td>${r.score}/${r.total}</td>`;
            table.appendChild(tr);
        });

        const sRes = await this.fetchApi('/api/stats');
        document.getElementById('adminStats').innerHTML = `
            <h1 style="font-size:3rem">${sRes.total}</h1>
            <p>Total Questions in Bank</p>
        `;
    },

    async addQuestion() {
        const payload = {
            text: document.getElementById('qText').value,
            optA: document.getElementById('qOptA').value,
            optB: document.getElementById('qOptB').value,
            optC: document.getElementById('qOptC').value,
            optD: document.getElementById('qOptD').value,
            correctIdx: document.getElementById('qCorrect').value,
            category: document.getElementById('qCat').value,
            difficulty: document.getElementById('qDiff').value
        };
        const res = await this.fetchApi('/api/questions', {method:'POST', body: JSON.stringify(payload)});
        if(res.success) { this.toast("Added!"); document.getElementById('qText').value = ''; this.loadAdminData(); }
    },

    // ==========================================
    // RENDER: Student Dashboard
    // ==========================================
    async renderStudent() {
        document.getElementById('app').innerHTML = `
        <div class="glass-card fade-in" style="max-width: 900px; margin:auto;">
            <div class="nav-bar">
                <h2>Hello, ${this.username}</h2>
                <button class="btn btn-secondary" style="width: auto; margin:0" onclick="App.logout()">Logout</button>
            </div>
            
            <div class="dashboard-grid">
                <div>
                    <h3>Take a Quiz</h3>
                    <div style="margin-top: 1rem;">
                        <button class="btn btn-primary" onclick="App.startQuiz('all')" style="margin-bottom:1rem">All Questions Full Quiz</button>
                        
                        <div class="glass-card" style="padding: 1.5rem; margin-bottom:1rem">
                            <label style="color:var(--text-muted); font-size:0.875rem">Difficulty</label>
                            <select id="selDiff" style="margin-top:0.5rem; margin-bottom:1rem"><option value="1">Easy</option><option value="2">Medium</option><option value="3">Hard</option></select>
                            <button class="btn btn-primary" onclick="App.startQuiz('difficulty')">Start Ranked Quiz</button>
                        </div>
                    </div>
                </div>
                <div>
                    <h3>My Past Results</h3>
                    <div style="overflow-x:auto;">
                        <table id="myResultsTable"><tr><th>Score</th><th>Grade</th><th>Time</th><th>Date</th></tr></table>
                    </div>
                </div>
            </div>
        </div>`;
        this.loadMyResults();
    },

    async loadMyResults() {
        const res = await this.fetchApi('/api/results');
        const myRes = res.filter(r => r.username === this.username);
        const t = document.getElementById('myResultsTable');
        myRes.forEach(r => {
            const tr = document.createElement('tr');
            tr.innerHTML = `<td>${r.score}/${r.total}</td><td><span class="grade-pill ${r.grade.includes('F') ? 'grade-bad' : 'grade-good'}">${r.grade}</span></td><td>${r.time}</td><td>${r.date.substring(0,10)}</td>`;
            t.appendChild(tr);
        });
    },

    async startQuiz(mode) {
        const payload = { mode };
        if(mode === 'difficulty') payload.difficulty = document.getElementById('selDiff').value;
        const qs = await this.fetchApi('/api/quiz/generate', {method:'POST', body: JSON.stringify(payload)});
        
        if(!qs || qs.length === 0) return this.toast("No questions found!");
        
        this.currentQuiz = { questions: qs, curIdx: 0, answers: [], qIds: qs.map(q => q.id), startTime: Date.now() };
        this.renderQuizStep();
    },

    renderQuizStep() {
        const q = this.currentQuiz.questions[this.currentQuiz.curIdx];
        if(!q) return this.submitQuiz();

        this.timeLeft = 30;

        document.getElementById('app').innerHTML = `
        <div class="glass-card slide-in" style="max-width: 700px; margin:auto;">
            <div style="display:flex; justify-content:space-between; align-items:center">
                <span style="color:var(--text-muted); font-weight:600">Question ${this.currentQuiz.curIdx + 1} of ${this.currentQuiz.questions.length}</span>
                <span style="background:rgba(255,255,255,0.1); padding:4px 12px; border-radius:99px; font-weight:600" id="timerLabel">⏱ 30s</span>
            </div>
            
            <div class="progress-container"><div class="progress-bar" id="pbar" style="width:100%"></div></div>
            
            <h2 style="margin: 2rem 0; font-size:1.5rem; line-height:1.4">${q.text}</h2>
            
            <div id="optionsContainer">
                ${q.options.map((opt, i) => `<button class="option-btn" onclick="App.selectOption(${i}, this)">${['A','B','C','D'][i]}. ${opt}</button>`).join('')}
            </div>
            
            <button class="btn btn-primary" style="margin-top: 2rem" onclick="App.nextQuestion()">Next Question</button>
        </div>`;

        this.startTimer();
    },

    startTimer() {
        clearInterval(this.timer);
        const pbar = document.getElementById('pbar');
        const lbl = document.getElementById('timerLabel');
        this.timer = setInterval(() => {
            this.timeLeft--;
            lbl.innerText = `⏱ ${this.timeLeft}s`;
            pbar.style.width = `${(this.timeLeft / 30) * 100}%`;
            if(this.timeLeft <= 0) this.nextQuestion();
        }, 1000);
    },

    selectOption(idx, btnElem) {
        document.querySelectorAll('.option-btn').forEach(b => b.classList.remove('selected'));
        btnElem.classList.add('selected');
        this.currentQuiz.pendingAnswer = idx;
    },

    nextQuestion() {
        clearInterval(this.timer);
        const ans = this.currentQuiz.pendingAnswer !== undefined ? this.currentQuiz.pendingAnswer : -1;
        this.currentQuiz.answers.push(ans);
        this.currentQuiz.pendingAnswer = undefined;
        this.currentQuiz.curIdx++;
        this.renderQuizStep();
    },

    async submitQuiz() {
        clearInterval(this.timer);
        const elapsed = Math.floor((Date.now() - this.currentQuiz.startTime) / 1000);
        document.getElementById('app').innerHTML = `<div class="glass-card" style="text-align:center"><h2>Calculating your score...</h2></div>`;
        
        const payload = {
            elapsed, 
            answers: this.currentQuiz.answers,
            questionIds: this.currentQuiz.qIds
        };
        
        try {
            const res = await this.fetchApi('/api/quiz/submit', {method: 'POST', body: JSON.stringify(payload)});
            document.getElementById('app').innerHTML = `
            <div class="glass-card fade-in" style="max-width: 600px; margin:auto; text-align:center">
                <h2 style="margin-bottom:0.5rem">Quiz Completed!</h2>
                <h1 style="font-size:4rem; margin:1rem 0">${res.score} / ${res.total}</h1>
                <p style="font-size:1.25rem; font-weight:600; color:var(--text-main)">Grade: <span class="grade-pill ${res.grade.includes('F') ? 'grade-bad':'grade-good'}" style="font-size:1.25rem">${res.grade}</span> (${res.percentage}%)</p>
                <div style="margin-top:3rem">
                    <button class="btn btn-primary" onclick="App.renderStudent()">Back to Dashboard</button>
                </div>
            </div>`;
        } catch(e) { this.toast("Failed to submit result"); }
    }
};

window.onload = () => App.init();
