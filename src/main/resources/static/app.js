(function () {
    const state = {
        mode: "chat",
        recentRequests: loadRecentRequests(),
        resultItems: [],
        activeResultIndex: -1
    };

    const modeMeta = {
        chat: {
            title: "课设对话",
            chip: "对话模式",
            primaryLabel: "课设问题",
            primaryPlaceholder: "例如：帮我推荐 3 个适合 Java Web 的课设题目",
            secondaryLabel: "补充要求",
            secondaryPlaceholder: "例如：希望偏简单、方便写报告、适合本科答辩",
            submitText: "发送对话",
            streamText: "流式对话"
        },
        project: {
            title: "完整方案",
            chip: "PAR 模式",
            primaryLabel: "课设需求",
            primaryPlaceholder: "描述你希望这个课设实现什么、包含哪些模块、面向什么场景",
            secondaryLabel: "附加要求",
            secondaryPlaceholder: "例如：要求适合本科软件课设答辩，包含数据库、接口、测试与报告建议",
            submitText: "生成完整方案",
            streamText: "流式生成方案"
        },
        artifact: {
            title: "产物生成",
            chip: "单产物模式",
            primaryLabel: "产物要求",
            primaryPlaceholder: "说明你需要生成的产物重点，比如数据库设计要包含哪些核心表",
            secondaryLabel: "已有规划或补充信息",
            secondaryPlaceholder: "可粘贴已有课设方案、老师要求或之前的规划结果",
            submitText: "生成产物",
            streamText: "当前模式不支持流式"
        },
        review: {
            title: "方案审查",
            chip: "审查模式",
            primaryLabel: "待审查内容",
            primaryPlaceholder: "把你现有的课设方案、报告片段或规划内容贴到这里",
            secondaryLabel: "补充说明",
            secondaryPlaceholder: "例如：请重点看数据库设计是否完整、答辩材料是否够用",
            submitText: "开始审查",
            streamText: "当前模式不支持流式"
        }
    };

    const elements = {
        modeNav: document.getElementById("mode-nav"),
        workspaceTitle: document.getElementById("workspace-title"),
        currentModeChip: document.getElementById("current-mode-chip"),
        requestStatusChip: document.getElementById("request-status-chip"),
        topicInput: document.getElementById("topic-input"),
        directionInput: document.getElementById("direction-input"),
        techStackInput: document.getElementById("tech-stack-input"),
        primaryLabel: document.getElementById("primary-label"),
        primaryInput: document.getElementById("primary-input"),
        secondaryField: document.getElementById("secondary-field"),
        secondaryLabel: document.getElementById("secondary-label"),
        secondaryInput: document.getElementById("secondary-input"),
        artifactOptionsRow: document.getElementById("artifact-options-row"),
        reviewTypeWrapper: document.getElementById("review-type-wrapper"),
        artifactTypeSelect: document.getElementById("artifact-type-select"),
        reviewTypeSelect: document.getElementById("review-type-select"),
        submitButton: document.getElementById("submit-button"),
        streamButton: document.getElementById("stream-button"),
        resetFormButton: document.getElementById("reset-form-button"),
        copyMainOutput: document.getElementById("copy-main-output"),
        clearMainOutput: document.getElementById("clear-main-output"),
        copySideOutput: document.getElementById("copy-side-output"),
        mainOutput: document.getElementById("main-output"),
        resultIndex: document.getElementById("result-index"),
        resultDetail: document.getElementById("result-detail"),
        recentList: document.getElementById("recent-list"),
        summaryMode: document.getElementById("summary-mode"),
        summaryTopic: document.getElementById("summary-topic"),
        summaryDirection: document.getElementById("summary-direction"),
        summaryTechStack: document.getElementById("summary-tech-stack"),
        streamStateLabel: document.getElementById("stream-state-label"),
        quickSecondHand: document.getElementById("quick-second-hand"),
        quickScoreSystem: document.getElementById("quick-score-system"),
        quickReport: document.getElementById("quick-report"),
        quickReview: document.getElementById("quick-review")
    };

    bindEvents();
    applyMode(state.mode);
    renderRecentRequests();
    updateSummary();

    function bindEvents() {
        elements.modeNav.querySelectorAll(".nav-item").forEach((button) => {
            button.addEventListener("click", () => applyMode(button.dataset.mode));
        });

        elements.submitButton.addEventListener("click", handleSubmit);
        elements.streamButton.addEventListener("click", handleStreamSubmit);
        elements.resetFormButton.addEventListener("click", resetForm);
        elements.copyMainOutput.addEventListener("click", () => copyText(elements.mainOutput.innerText));
        elements.copySideOutput.addEventListener("click", () => copyText(elements.resultDetail.innerText));
        elements.clearMainOutput.addEventListener("click", () => setMainOutput(emptyStateHtml()));

        [elements.topicInput, elements.directionInput, elements.techStackInput].forEach((input) => {
            input.addEventListener("input", updateSummary);
        });

        elements.quickSecondHand.addEventListener("click", () => {
            applyMode("project");
            elements.topicInput.value = "校园二手交易平台";
            elements.directionInput.value = "Java Web";
            elements.techStackInput.value = "Spring Boot, Vue, MySQL";
            elements.primaryInput.value = "要求包含用户、商品、订单、评价和后台管理模块，并适合软件课设答辩。";
            elements.secondaryInput.value = "需要包含数据库设计、接口设计、项目结构、测试建议和报告写作建议。";
            updateSummary();
        });

        elements.quickScoreSystem.addEventListener("click", () => {
            applyMode("project");
            elements.topicInput.value = "学生成绩管理系统";
            elements.directionInput.value = "Java Web";
            elements.techStackInput.value = "Spring Boot, Thymeleaf, MySQL";
            elements.primaryInput.value = "需要包含学生、课程、成绩、教师和统计分析功能。";
            elements.secondaryInput.value = "要求适合课程设计报告编写和本科答辩展示。";
            updateSummary();
        });

        elements.quickReport.addEventListener("click", () => {
            applyMode("artifact");
            elements.artifactTypeSelect.value = "report_outline";
            elements.topicInput.value = "校园二手交易平台";
            elements.primaryInput.value = "生成一份适合本科软件课设提交的课程设计报告大纲。";
            elements.secondaryInput.value = "希望重点覆盖需求分析、数据库设计、接口设计、测试与答辩准备。";
            updateSummary();
        });

        elements.quickReview.addEventListener("click", () => {
            applyMode("review");
            elements.topicInput.value = "学生成绩管理系统";
            elements.primaryInput.value = "当前方案包含需求分析、功能模块、数据库设计和部分接口说明，但还没有测试用例和答辩准备。";
            elements.secondaryInput.value = "请重点检查完整性和答辩材料准备情况。";
            updateSummary();
        });
    }

    function applyMode(mode) {
        state.mode = mode;
        const meta = modeMeta[mode];
        elements.workspaceTitle.textContent = meta.title;
        elements.currentModeChip.textContent = meta.chip;
        elements.summaryMode.textContent = meta.title;
        elements.primaryLabel.querySelector("span").textContent = meta.primaryLabel;
        elements.primaryInput.placeholder = meta.primaryPlaceholder;
        elements.secondaryLabel.textContent = meta.secondaryLabel;
        elements.secondaryInput.placeholder = meta.secondaryPlaceholder;
        elements.submitButton.textContent = meta.submitText;
        elements.streamButton.textContent = meta.streamText;
        elements.streamButton.disabled = mode === "artifact" || mode === "review";
        elements.streamStateLabel.textContent = meta.title;

        elements.artifactOptionsRow.style.display = mode === "chat" || mode === "project" ? "none" : "flex";
        elements.reviewTypeWrapper.style.display = mode === "review" ? "flex" : "none";

        elements.modeNav.querySelectorAll(".nav-item").forEach((button) => {
            button.classList.toggle("active", button.dataset.mode === mode);
        });
        updateSummary();
    }

    async function handleSubmit() {
        try {
            setStatus("处理中", "status-warning");
            switch (state.mode) {
                case "chat":
                    await submitChat();
                    break;
                case "project":
                    await submitProject();
                    break;
                case "artifact":
                    await submitArtifact();
                    break;
                case "review":
                    await submitReview();
                    break;
                default:
                    break;
            }
        } catch (error) {
            setStatus("执行失败", "status-danger");
            setMainOutput(renderMarkdown("## 请求失败\n\n" + escapeHtml(error.message || "未知错误")));
        }
    }

    async function handleStreamSubmit() {
        if (state.mode === "artifact" || state.mode === "review") {
            return;
        }

        try {
            setStatus("流式处理中", "status-warning");
            clearResultPanels();
            const endpoint = state.mode === "chat" ? "/api/course_chat_stream" : "/api/course_project_stream";
            const payload = state.mode === "chat" ? buildChatPayload() : buildProjectPayload();
            await streamSse(endpoint, payload, handleStreamEvent);
            setStatus("流式完成", "status-success");
        } catch (error) {
            setStatus("流式失败", "status-danger");
            appendToMainOutput("\n\n## 流式请求失败\n\n" + escapeHtml(error.message || "未知错误"));
        }
    }

    async function submitChat() {
        const payload = buildChatPayload();
        const response = await requestJson("/api/course_chat", payload);
        const answer = response.data.answer;
        setMainOutput(renderMarkdown(answer));
        setStructuredResults([
            { key: "课设回答", content: answer },
            { key: "会话编号", content: response.data.conversationId }
        ]);
        addRecentRequest("课设对话", payload.message);
        setStatus("已完成", "status-success");
    }

    async function submitProject() {
        const payload = buildProjectPayload();
        const response = await requestJson("/api/course_project", payload);
        const projectData = response.data;
        const combined = [
            "## Supervisor 阶段输出",
            projectData.supervisorResult,
            "## Planner 阶段输出",
            projectData.plannerResult,
            "## Executor 阶段输出",
            projectData.executorResult
        ].join("\n\n");
        setMainOutput(renderMarkdown(combined));
        setStructuredResults([
            { key: "Supervisor", content: projectData.supervisorResult },
            { key: "Planner", content: projectData.plannerResult },
            { key: "Executor", content: projectData.executorResult }
        ]);
        addRecentRequest("完整方案", payload.topic);
        setStatus("已完成", "status-success");
    }

    async function submitArtifact() {
        const payload = buildArtifactPayload();
        const response = await requestJson("/api/course_project/artifact", payload);
        setMainOutput(renderMarkdown(response.data.content));
        setStructuredResults([
            { key: readableArtifactType(response.data.artifactType), content: response.data.content }
        ]);
        addRecentRequest("产物生成", payload.artifactType + " / " + payload.topic);
        setStatus("已完成", "status-success");
    }

    async function submitReview() {
        const payload = buildReviewPayload();
        const response = await requestJson("/api/course_project/review", payload);
        setMainOutput(renderMarkdown(response.data.reviewResult));
        setStructuredResults([
            { key: "审查结果", content: response.data.reviewResult }
        ]);
        addRecentRequest("方案审查", payload.topic);
        setStatus("已完成", "status-success");
    }

    function buildChatPayload() {
        const topic = ensureField(elements.topicInput.value, "课设题目");
        const message = ensureField(elements.primaryInput.value, "课设问题");
        const extra = elements.secondaryInput.value.trim();
        return {
            conversationId: "chat-" + Date.now(),
            message: extra ? `${message}\n\n课设题目：${topic}\n补充要求：${extra}` : `${message}\n\n课设题目：${topic}`,
            useTools: true
        };
    }

    function buildProjectPayload() {
        return {
            requestId: "project-" + Date.now(),
            topic: ensureField(elements.topicInput.value, "课设题目"),
            direction: elements.directionInput.value.trim(),
            requirements: ensureField(elements.primaryInput.value, "课设需求"),
            techStack: elements.techStackInput.value.trim(),
            useTools: true
        };
    }

    function buildArtifactPayload() {
        return {
            requestId: "artifact-" + Date.now(),
            topic: ensureField(elements.topicInput.value, "课设题目"),
            artifactType: elements.artifactTypeSelect.value,
            direction: elements.directionInput.value.trim(),
            requirements: ensureField(elements.primaryInput.value, "产物要求"),
            techStack: elements.techStackInput.value.trim(),
            existingPlan: elements.secondaryInput.value.trim()
        };
    }

    function buildReviewPayload() {
        return {
            requestId: "review-" + Date.now(),
            topic: ensureField(elements.topicInput.value, "课设题目"),
            content: ensureField(elements.primaryInput.value, "待审查内容"),
            reviewType: elements.reviewTypeSelect.value
        };
    }

    async function requestJson(url, payload) {
        const response = await fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || "请求失败");
        }
        return data;
    }

    async function streamSse(url, payload, onEvent) {
        const response = await fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        if (!response.ok || !response.body) {
            throw new Error("流式请求失败");
        }
        setMainOutput("");
        const reader = response.body.getReader();
        const decoder = new TextDecoder("utf-8");
        let buffer = "";
        while (true) {
            const { value, done } = await reader.read();
            if (done) {
                break;
            }
            buffer += decoder.decode(value, { stream: true });
            const segments = buffer.split("\n\n");
            buffer = segments.pop() || "";
            segments.forEach((segment) => {
                if (segment.trim()) {
                    onEvent(parseSseEvent(segment));
                }
            });
        }
    }

    function handleStreamEvent(event) {
        if (!event.event || event.event === "message") {
            appendToMainOutput(renderInlineMarkdown(event.data || ""));
            return;
        }
        if (event.event === "conversation" || event.event === "request") {
            try {
                const parsed = JSON.parse(event.data);
                if (parsed.conversationId) {
                    setStructuredResults([{ key: "会话编号", content: parsed.conversationId }]);
                }
                if (parsed.requestId) {
                    setStructuredResults([{ key: "请求编号", content: parsed.requestId }]);
                }
            } catch (error) {
                // ignore parse failures
            }
            return;
        }
        if (event.event === "done") {
            const fullText = elements.mainOutput.innerText.trim();
            if (fullText) {
                setStructuredResults(buildResultsFromText(fullText));
            }
            return;
        }
        if (event.event === "error") {
            throw new Error(event.data || "流式响应出错");
        }
    }

    function parseSseEvent(segment) {
        const lines = segment.split("\n");
        const event = { event: "", data: "" };
        lines.forEach((line) => {
            if (line.startsWith("event:")) {
                event.event = line.slice(6).trim();
            } else if (line.startsWith("data:")) {
                event.data += line.slice(5).trim();
            }
        });
        return event;
    }

    function setMainOutput(html) {
        elements.mainOutput.innerHTML = html || "";
    }

    function appendToMainOutput(htmlFragment) {
        if (elements.mainOutput.querySelector(".empty-state")) {
            elements.mainOutput.innerHTML = "";
        }
        elements.mainOutput.insertAdjacentHTML("beforeend", htmlFragment);
        elements.mainOutput.scrollTop = elements.mainOutput.scrollHeight;
    }

    function setStructuredResults(items) {
        state.resultItems = items;
        state.activeResultIndex = items.length ? 0 : -1;
        renderResultIndex();
        renderResultDetail();
    }

    function clearResultPanels() {
        setStructuredResults([]);
    }

    function renderResultIndex() {
        if (!state.resultItems.length) {
            elements.resultIndex.innerHTML = '<div class="empty-hint">生成完成后，这里会显示结构化条目。</div>';
            return;
        }
        elements.resultIndex.innerHTML = state.resultItems.map((item, index) => `
            <button class="result-index-item ${index === state.activeResultIndex ? "active" : ""}" data-index="${index}">
                ${escapeHtml(item.key)}
            </button>
        `).join("");
        elements.resultIndex.querySelectorAll(".result-index-item").forEach((button) => {
            button.addEventListener("click", () => {
                state.activeResultIndex = Number(button.dataset.index);
                renderResultIndex();
                renderResultDetail();
            });
        });
    }

    function renderResultDetail() {
        if (state.activeResultIndex < 0 || !state.resultItems.length) {
            elements.resultDetail.innerHTML = '<div class="empty-hint">右侧详情区等待结果填充。</div>';
            return;
        }
        elements.resultDetail.innerHTML = renderMarkdown(state.resultItems[state.activeResultIndex].content);
    }

    function buildResultsFromText(text) {
        const sections = text.split(/\n(?=##\s)/).map((item) => item.trim()).filter(Boolean);
        if (!sections.length) {
            return [{ key: "主输出", content: text }];
        }
        return sections.map((section, index) => {
            const titleMatch = section.match(/^##\s+(.+)$/m);
            return {
                key: titleMatch ? titleMatch[1].trim() : `结果 ${index + 1}`,
                content: section
            };
        });
    }

    function addRecentRequest(type, summary) {
        state.recentRequests.unshift({
            type,
            summary,
            time: new Date().toLocaleTimeString("zh-CN", { hour: "2-digit", minute: "2-digit" })
        });
        state.recentRequests = state.recentRequests.slice(0, 8);
        localStorage.setItem("keshetong-recent-requests", JSON.stringify(state.recentRequests));
        renderRecentRequests();
    }

    function renderRecentRequests() {
        if (!state.recentRequests.length) {
            elements.recentList.innerHTML = '<div class="empty-hint">还没有历史请求</div>';
            return;
        }
        elements.recentList.innerHTML = state.recentRequests.map((item) => `
            <div class="recent-item">
                <strong>${escapeHtml(item.type)}</strong>
                <span>${escapeHtml(item.summary)}</span>
                <span>${escapeHtml(item.time)}</span>
            </div>
        `).join("");
    }

    function updateSummary() {
        const currentMode = modeMeta[state.mode].title;
        elements.summaryMode.textContent = currentMode;
        elements.summaryTopic.textContent = elements.topicInput.value.trim() || "未填写";
        elements.summaryDirection.textContent = elements.directionInput.value.trim() || "未填写";
        elements.summaryTechStack.textContent = elements.techStackInput.value.trim() || "未填写";
    }

    function setStatus(text, className) {
        elements.requestStatusChip.textContent = text;
        elements.requestStatusChip.classList.remove("status-success", "status-warning", "status-danger");
        if (className) {
            elements.requestStatusChip.classList.add(className);
        }
    }

    function resetForm() {
        elements.topicInput.value = "";
        elements.directionInput.value = "";
        elements.techStackInput.value = "";
        elements.primaryInput.value = "";
        elements.secondaryInput.value = "";
        updateSummary();
    }

    function renderMarkdown(text) {
        if (!text) {
            return '<div class="empty-hint">暂无内容</div>';
        }
        let html = escapeHtml(text);
        html = html.replace(/```([\s\S]*?)```/g, (_, code) => `<pre><code>${code.trim()}</code></pre>`);
        html = html.replace(/^### (.*)$/gm, "<h3>$1</h3>");
        html = html.replace(/^## (.*)$/gm, "<h2>$1</h2>");
        html = html.replace(/^# (.*)$/gm, "<h1>$1</h1>");
        html = html.replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>");
        html = html.replace(/`([^`]+)`/g, "<code>$1</code>");
        html = html.replace(/^- (.*)$/gm, "<li>$1</li>");
        html = html.replace(/(<li>.*<\/li>)/gs, "<ul>$1</ul>");
        html = html.replace(/\n{2,}/g, "</p><p>");
        html = "<p>" + html + "</p>";
        html = html.replace(/<p>\s*(<h[1-3]>)/g, "$1");
        html = html.replace(/(<\/h[1-3]>)\s*<\/p>/g, "$1");
        html = html.replace(/<p>\s*(<ul>)/g, "$1");
        html = html.replace(/(<\/ul>)\s*<\/p>/g, "$1");
        html = html.replace(/<p>\s*(<pre>)/g, "$1");
        html = html.replace(/(<\/pre>)\s*<\/p>/g, "$1");
        return `<div class="markdown-body">${html}</div>`;
    }

    function renderInlineMarkdown(text) {
        return `<div class="markdown-body"><p>${escapeHtml(text).replace(/\n/g, "<br>")}</p></div>`;
    }

    function emptyStateHtml() {
        return `
            <div class="empty-state">
                <h3>输出区已清空</h3>
                <p>重新发起请求后，这里会显示新的课设对话、完整方案或产物结果。</p>
            </div>
        `;
    }

    function readableArtifactType(type) {
        const mapping = {
            database_design: "数据库设计",
            api_design: "接口设计",
            project_structure: "项目结构",
            report_outline: "报告大纲",
            report_draft: "报告初稿",
            test_cases: "测试用例",
            defense_qa: "答辩问答"
        };
        return mapping[type] || type;
    }

    function ensureField(value, label) {
        if (!value || !value.trim()) {
            throw new Error(label + "不能为空");
        }
        return value.trim();
    }

    function copyText(text) {
        if (!text.trim()) {
            return;
        }
        navigator.clipboard.writeText(text).then(() => {
            setStatus("已复制", "status-success");
        }).catch(() => {
            setStatus("复制失败", "status-danger");
        });
    }

    function escapeHtml(text) {
        return String(text)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }

    function loadRecentRequests() {
        try {
            const raw = localStorage.getItem("keshetong-recent-requests");
            return raw ? JSON.parse(raw) : [];
        } catch (error) {
            return [];
        }
    }
})();
