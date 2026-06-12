(function () {
    const RECENT_REQUESTS_KEY = "course-agent-recent-requests";
    const KNOWLEDGE_RECORDS_KEY = "course-agent-knowledge-records";

    const state = {
        mode: "chat",
        drawerOpen: false,
        drawerView: "results",
        recentRequests: loadJson(RECENT_REQUESTS_KEY, []),
        knowledgeRecords: loadJson(KNOWLEDGE_RECORDS_KEY, []),
        resultItems: [],
        activeResultIndex: -1
    };

    const modeMeta = {
        chat: {
            title: "课设对话",
            chip: "对话模式",
            primaryLabel: "课设问题",
            primaryPlaceholder: "例如：请帮我推荐三个适合 Java Web 的软件课设题目，并说明难度差异",
            secondaryLabel: "补充要求",
            secondaryPlaceholder: "例如：希望适合本科答辩、报告好写、功能不要太复杂",
            submitText: "发送对话",
            streamText: "流式对话",
            showSecondary: true,
            streamEnabled: true
        },
        project: {
            title: "PAR 完整方案",
            chip: "PAR 模式",
            primaryLabel: "课设需求",
            primaryPlaceholder: "描述题目、核心模块、老师要求和你希望最后自动生成的内容",
            secondaryLabel: "附加说明",
            secondaryPlaceholder: "例如：请同时给出数据库设计、接口设计、项目结构、报告大纲、测试用例和答辩问答",
            submitText: "生成完整方案",
            streamText: "流式生成",
            showSecondary: true,
            streamEnabled: true
        },
        review: {
            title: "方案审查",
            chip: "审查模式",
            primaryLabel: "待审查内容",
            primaryPlaceholder: "把你现有的课设方案、报告节选、数据库设计或接口说明贴进来",
            secondaryLabel: "审查重点",
            secondaryPlaceholder: "例如：请重点检查数据库设计是否完整、测试用例是否够用、答辩材料是否充分",
            submitText: "开始审查",
            streamText: "当前模式不支持流式",
            showSecondary: true,
            streamEnabled: false
        }
    };

    const artifactMeta = {
        database_design: "数据库设计",
        api_design: "接口设计",
        project_structure: "项目结构",
        report_outline: "报告大纲",
        report_draft: "报告初稿",
        test_cases: "测试用例",
        defense_qa: "答辩问答"
    };

    const elements = {
        appShell: document.querySelector(".app-shell"),
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
        submitButton: document.getElementById("submit-button"),
        streamButton: document.getElementById("stream-button"),
        resetFormButton: document.getElementById("reset-form-button"),
        uploadButton: document.getElementById("upload-button"),
        knowledgeUploadButton: document.getElementById("knowledge-upload-button"),
        fileInput: document.getElementById("file-input"),
        toggleDrawerButton: document.getElementById("toggle-drawer-button"),
        closeDrawerButton: document.getElementById("close-drawer-button"),
        openKnowledgeButton: document.getElementById("open-knowledge-button"),
        copySideOutput: document.getElementById("copy-side-output"),
        clearMainOutput: document.getElementById("clear-main-output"),
        mainOutput: document.getElementById("main-output"),
        recentList: document.getElementById("recent-list"),
        resultIndex: document.getElementById("result-index"),
        resultDetail: document.getElementById("result-detail"),
        summaryMode: document.getElementById("summary-mode"),
        summaryTopic: document.getElementById("summary-topic"),
        summaryDirection: document.getElementById("summary-direction"),
        summaryTechStack: document.getElementById("summary-tech-stack"),
        composerModeTag: document.getElementById("composer-mode-tag"),
        composerTopicTag: document.getElementById("composer-topic-tag"),
        composerStackTag: document.getElementById("composer-stack-tag"),
        drawerTitle: document.getElementById("drawer-title"),
        drawerSubtitle: document.getElementById("drawer-subtitle"),
        drawerTabs: document.querySelectorAll(".drawer-tab"),
        drawerViews: {
            results: document.getElementById("drawer-results-view"),
            knowledge: document.getElementById("drawer-knowledge-view")
        },
        knowledgeStatus: document.getElementById("knowledge-status"),
        knowledgeList: document.getElementById("knowledge-list"),
        quickSecondHand: document.getElementById("quick-second-hand"),
        quickScoreSystem: document.getElementById("quick-score-system"),
        quickReview: document.getElementById("quick-review"),
        newChatButton: document.getElementById("new-chat-button"),
        searchButton: document.getElementById("search-button"),
        artifactQuickButtons: Array.from(document.querySelectorAll("[data-artifact]"))
    };

    bindEvents();
    applyMode(state.mode);
    renderRecentRequests();
    renderKnowledgeRecords();
    updateSummary();

    function bindEvents() {
        elements.modeNav.querySelectorAll(".mode-item").forEach((button) => {
            button.addEventListener("click", () => applyMode(button.dataset.mode));
        });

        [elements.topicInput, elements.directionInput, elements.techStackInput].forEach((input) => {
            input.addEventListener("input", updateSummary);
        });

        elements.submitButton.addEventListener("click", handleSubmit);
        elements.streamButton.addEventListener("click", handleStreamSubmit);
        elements.resetFormButton.addEventListener("click", resetForm);
        elements.copySideOutput.addEventListener("click", () => copyText(elements.resultDetail.innerText));
        elements.clearMainOutput.addEventListener("click", clearMessages);

        elements.uploadButton.addEventListener("click", () => elements.fileInput.click());
        elements.knowledgeUploadButton.addEventListener("click", () => elements.fileInput.click());
        elements.fileInput.addEventListener("change", handleFileUpload);
        elements.openKnowledgeButton.addEventListener("click", () => openDrawer("knowledge"));
        elements.toggleDrawerButton.addEventListener("click", () => {
            if (state.drawerOpen) {
                closeDrawer();
            } else {
                openDrawer("results");
            }
        });
        elements.closeDrawerButton.addEventListener("click", closeDrawer);

        elements.drawerTabs.forEach((tab) => {
            tab.addEventListener("click", () => setDrawerView(tab.dataset.drawerView));
        });

        elements.quickSecondHand.addEventListener("click", fillSecondHandCase);
        elements.quickScoreSystem.addEventListener("click", fillScoreSystemCase);
        elements.quickReview.addEventListener("click", fillReviewCase);
        elements.newChatButton.addEventListener("click", resetConversation);
        elements.searchButton.addEventListener("click", () => appendSystemHint("搜索功能当前先保留界面入口，后续会接入真实会话检索。"));

        elements.artifactQuickButtons.forEach((button) => {
            button.addEventListener("click", () => generateQuickArtifact(button.dataset.artifact));
        });
    }

    function applyMode(mode) {
        state.mode = mode;
        const meta = modeMeta[mode];
        elements.workspaceTitle.textContent = meta.title;
        elements.currentModeChip.textContent = meta.chip;
        elements.primaryLabel.textContent = meta.primaryLabel;
        elements.primaryInput.placeholder = meta.primaryPlaceholder;
        elements.secondaryLabel.textContent = meta.secondaryLabel;
        elements.secondaryInput.placeholder = meta.secondaryPlaceholder;
        elements.submitButton.textContent = meta.submitText;
        elements.streamButton.textContent = meta.streamText;
        elements.streamButton.disabled = !meta.streamEnabled;
        elements.secondaryField.style.display = meta.showSecondary ? "flex" : "none";

        elements.modeNav.querySelectorAll(".mode-item").forEach((button) => {
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
                case "review":
                    await submitReview();
                    break;
                default:
                    break;
            }
        } catch (error) {
            setStatus("执行失败", "status-danger");
            appendMessage("assistant", "请求失败", "## 请求失败\n\n" + escapeHtml(error.message || "未知错误"));
        }
    }

    async function handleStreamSubmit() {
        if (!modeMeta[state.mode].streamEnabled) {
            return;
        }
        try {
            setStatus("流式处理中", "status-warning");
            const endpoint = state.mode === "chat" ? "/api/course_chat_stream" : "/api/course_project_stream";
            const payload = state.mode === "chat" ? buildChatPayload() : buildProjectPayload();
            const title = state.mode === "chat" ? "流式课设对话" : "流式 PAR 完整方案";
            const messageElement = appendMessage("assistant", title, "");
            clearResultPanels();
            await streamSse(endpoint, payload, (event) => handleStreamEvent(event, messageElement));
            addRecentRequest(state.mode === "chat" ? "课设对话" : "PAR 完整方案", payload.topic || payload.message);
            setStatus("流式完成", "status-success");
        } catch (error) {
            setStatus("流式失败", "status-danger");
            appendMessage("assistant", "流式请求失败", "## 流式请求失败\n\n" + escapeHtml(error.message || "未知错误"));
        }
    }

    async function submitChat() {
        const payload = buildChatPayload();
        appendUserMessage(payload.message);
        const response = await requestJson("/api/course_chat", payload);
        appendMessage("assistant", "课设回答", response.data.answer);
        setStructuredResults([
            { key: "课设回答", content: response.data.answer },
            { key: "会话编号", content: response.data.conversationId }
        ]);
        addRecentRequest("课设对话", payload.message);
        setStatus("已完成", "status-success");
    }

    async function submitProject() {
        const payload = buildProjectPayload();
        appendUserMessage(payload.requirements);
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
        appendMessage("assistant", "PAR 完整方案", combined);
        setStructuredResults([
            { key: "Supervisor", content: projectData.supervisorResult },
            { key: "Planner", content: projectData.plannerResult },
            { key: "Executor", content: projectData.executorResult }
        ]);
        appendArtifactSummary([
            { title: "数据库设计", content: "当前版本会在下一阶段合并为 PAR 自动产物。你也可以先用快捷按钮单独补生成。" },
            { title: "接口设计", content: "当前前端已为 PAR 聚合结果预留抽屉展示位，后续会接入完整自动联动结果。" },
            { title: "报告与答辩", content: "现阶段可先使用单独产物快捷入口，下一步会把报告大纲、测试用例和答辩问答并入主流程。" }
        ]);
        addRecentRequest("PAR 完整方案", payload.topic);
        openDrawer("results");
        setStatus("已完成", "status-success");
    }

    async function submitReview() {
        const payload = buildReviewPayload();
        appendUserMessage(payload.content);
        const response = await requestJson("/api/course_project/review", payload);
        appendMessage("assistant", "方案审查结果", response.data.reviewResult);
        setStructuredResults([{ key: "审查结果", content: response.data.reviewResult }]);
        addRecentRequest("方案审查", payload.topic);
        openDrawer("results");
        setStatus("已完成", "status-success");
    }

    async function generateQuickArtifact(artifactType) {
        try {
            setStatus("产物生成中", "status-warning");
            const payload = buildArtifactPayload(artifactType);
            appendUserMessage("请单独生成：" + readableArtifactType(artifactType));
            const response = await requestJson("/api/course_project/artifact", payload);
            const title = readableArtifactType(response.data.artifactType);
            appendMessage("assistant", title, response.data.content);
            setStructuredResults([{ key: title, content: response.data.content }]);
            addRecentRequest("单项产物", title + " / " + payload.topic);
            openDrawer("results");
            setStatus("已完成", "status-success");
        } catch (error) {
            setStatus("产物生成失败", "status-danger");
            appendMessage("assistant", "产物生成失败", "## 产物生成失败\n\n" + escapeHtml(error.message || "未知错误"));
        }
    }

    async function handleFileUpload(event) {
        const file = event.target.files && event.target.files[0];
        if (!file) {
            return;
        }
        try {
            setStatus("文档上传中", "status-warning");
            openDrawer("knowledge");
            const formData = new FormData();
            formData.append("file", file);
            const response = await fetch("/api/upload", {
                method: "POST",
                body: formData
            });
            const data = await response.json();
            if (!response.ok) {
                throw new Error(data.message || "上传失败");
            }
            const record = {
                fileName: data.data.fileName,
                filePath: data.data.filePath,
                fileSize: data.data.fileSize,
                uploadedAt: new Date().toLocaleString("zh-CN"),
                status: "文件已上传，知识库入库结果请结合后端日志与后续检索效果确认"
            };
            state.knowledgeRecords.unshift(record);
            state.knowledgeRecords = state.knowledgeRecords.slice(0, 12);
            saveJson(KNOWLEDGE_RECORDS_KEY, state.knowledgeRecords);
            renderKnowledgeRecords();
            appendSystemHint("知识库文档已上传：" + record.fileName + "。当前版本后端会继续执行分片、向量化和入库。");
            setStatus("上传完成", "status-success");
        } catch (error) {
            setStatus("上传失败", "status-danger");
            renderKnowledgeStatus("上传失败：" + (error.message || "未知错误"));
            appendMessage("assistant", "知识库上传失败", "## 知识库上传失败\n\n" + escapeHtml(error.message || "未知错误"));
        } finally {
            elements.fileInput.value = "";
        }
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

    function buildArtifactPayload(artifactType) {
        return {
            requestId: "artifact-" + Date.now(),
            topic: ensureField(elements.topicInput.value, "课设题目"),
            artifactType: artifactType,
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
            reviewType: "completeness"
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

        const reader = response.body.getReader();
        const decoder = new TextDecoder("utf-8");
        let buffer = "";

        while (true) {
            const result = await reader.read();
            if (result.done) {
                break;
            }
            buffer += decoder.decode(result.value, { stream: true });
            const events = buffer.split("\n\n");
            buffer = events.pop() || "";
            events.forEach((segment) => {
                if (segment.trim()) {
                    onEvent(parseSseEvent(segment));
                }
            });
        }
    }

    function handleStreamEvent(event, messageElement) {
        if (event.event === "request") {
            return;
        }
        if (event.event === "done") {
            return;
        }
        if (event.event === "error") {
            throw new Error(event.data || "流式请求失败");
        }
        appendToMessage(messageElement, event.data || "");
    }

    function appendUserMessage(content) {
        appendMessage("user", "你的输入", content);
    }

    function appendSystemHint(content) {
        appendMessage("assistant", "系统提示", content);
    }

    function appendArtifactSummary(items) {
        const wrapper = document.createElement("div");
        wrapper.className = "artifact-summary-grid";
        items.forEach((item) => {
            const card = document.createElement("div");
            card.className = "artifact-summary-card";
            card.innerHTML = `<h4>${escapeHtml(item.title)}</h4><p>${escapeHtml(item.content)}</p>`;
            wrapper.appendChild(card);
        });
        elements.mainOutput.appendChild(wrapper);
        scrollToBottom();
    }

    function appendMessage(role, title, markdownText) {
        removeWelcomePanel();
        const messageCard = document.createElement("article");
        messageCard.className = "message-card " + role;

        const meta = document.createElement("div");
        meta.className = "message-meta";
        meta.innerHTML = `
            <span class="message-title">${escapeHtml(title)}</span>
            <span>${new Date().toLocaleTimeString("zh-CN", { hour: "2-digit", minute: "2-digit" })}</span>
        `;

        const body = document.createElement("div");
        body.className = "message-body markdown-body";
        body.innerHTML = renderMarkdown(markdownText);

        messageCard.appendChild(meta);
        messageCard.appendChild(body);
        elements.mainOutput.appendChild(messageCard);
        scrollToBottom();
        return body;
    }

    function appendToMessage(messageElement, text) {
        const current = messageElement.dataset.rawContent || "";
        messageElement.dataset.rawContent = current + text;
        messageElement.innerHTML = renderMarkdown(messageElement.dataset.rawContent);
        scrollToBottom();
    }

    function removeWelcomePanel() {
        const welcome = elements.mainOutput.querySelector(".welcome-panel");
        if (welcome) {
            welcome.remove();
        }
    }

    function clearMessages() {
        elements.mainOutput.innerHTML = "";
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
            elements.resultIndex.innerHTML = '<div class="drawer-empty">结果生成后会出现在这里。</div>';
            return;
        }
        elements.resultIndex.innerHTML = "";
        state.resultItems.forEach((item, index) => {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "result-index-item" + (index === state.activeResultIndex ? " active" : "");
            button.textContent = item.key;
            button.addEventListener("click", () => {
                state.activeResultIndex = index;
                renderResultIndex();
                renderResultDetail();
            });
            elements.resultIndex.appendChild(button);
        });
    }

    function renderResultDetail() {
        if (state.activeResultIndex < 0 || !state.resultItems[state.activeResultIndex]) {
            elements.resultDetail.innerHTML = '<div class="drawer-empty">等待选择结果条目。</div>';
            return;
        }
        elements.resultDetail.innerHTML = renderMarkdown(state.resultItems[state.activeResultIndex].content);
    }

    function renderKnowledgeRecords() {
        if (!state.knowledgeRecords.length) {
            renderKnowledgeStatus("还没有上传记录。");
            elements.knowledgeList.innerHTML = '<div class="drawer-empty">暂时没有知识库上传记录。</div>';
            return;
        }
        renderKnowledgeStatus("当前共记录 " + state.knowledgeRecords.length + " 条知识库上传记录。");
        elements.knowledgeList.innerHTML = "";
        state.knowledgeRecords.forEach((record) => {
            const item = document.createElement("div");
            item.className = "knowledge-record";
            item.innerHTML = `
                <div class="knowledge-record-title">${escapeHtml(record.fileName)}</div>
                <div class="knowledge-record-meta">
                    上传时间：${escapeHtml(record.uploadedAt)}<br>
                    文件大小：${escapeHtml(readableFileSize(record.fileSize))}<br>
                    状态：${escapeHtml(record.status)}
                </div>
            `;
            elements.knowledgeList.appendChild(item);
        });
    }

    function renderKnowledgeStatus(text) {
        elements.knowledgeStatus.textContent = text;
    }

    function renderRecentRequests() {
        if (!state.recentRequests.length) {
            elements.recentList.innerHTML = '<div class="history-empty">还没有历史会话</div>';
            return;
        }
        elements.recentList.innerHTML = "";
        state.recentRequests.forEach((item) => {
            const card = document.createElement("div");
            card.className = "history-card";
            card.innerHTML = `
                <div class="history-card-title">${escapeHtml(item.title)}</div>
                <div class="history-card-meta">${escapeHtml(item.time)}</div>
            `;
            elements.recentList.appendChild(card);
        });
    }

    function addRecentRequest(type, rawTitle) {
        const normalized = String(rawTitle || "").replace(/\s+/g, " ").trim();
        const title = normalized.length > 28 ? normalized.slice(0, 28) + "..." : normalized;
        state.recentRequests.unshift({
            title: type + " · " + (title || "未命名请求"),
            time: new Date().toLocaleString("zh-CN", { month: "numeric", day: "numeric", hour: "2-digit", minute: "2-digit" })
        });
        state.recentRequests = state.recentRequests.slice(0, 16);
        saveJson(RECENT_REQUESTS_KEY, state.recentRequests);
        renderRecentRequests();
    }

    function updateSummary() {
        const modeTitle = modeMeta[state.mode].title;
        const topic = elements.topicInput.value.trim() || "未填写";
        const direction = elements.directionInput.value.trim() || "未填写";
        const stack = elements.techStackInput.value.trim() || "未填写";

        elements.summaryMode.textContent = modeTitle;
        elements.summaryTopic.textContent = topic;
        elements.summaryDirection.textContent = direction;
        elements.summaryTechStack.textContent = stack;

        elements.composerModeTag.textContent = "当前模式：" + modeTitle;
        elements.composerTopicTag.textContent = "题目：" + topic;
        elements.composerStackTag.textContent = "技术栈：" + stack;
    }

    function setStatus(text, className) {
        elements.requestStatusChip.textContent = text;
        elements.requestStatusChip.classList.remove("status-success", "status-warning", "status-danger");
        if (className) {
            elements.requestStatusChip.classList.add(className);
        }
    }

    function openDrawer(view) {
        state.drawerOpen = true;
        elements.appShell.classList.add("drawer-open");
        setDrawerView(view || state.drawerView);
    }

    function closeDrawer() {
        state.drawerOpen = false;
        elements.appShell.classList.remove("drawer-open");
    }

    function setDrawerView(view) {
        state.drawerView = view;
        elements.drawerTabs.forEach((tab) => {
            tab.classList.toggle("active", tab.dataset.drawerView === view);
        });
        Object.keys(elements.drawerViews).forEach((key) => {
            elements.drawerViews[key].classList.toggle("active", key === view);
        });
        if (view === "knowledge") {
            elements.drawerTitle.textContent = "知识库面板";
            elements.drawerSubtitle.textContent = "查看上传记录、入库状态和后续 RAG 增强入口。";
        } else {
            elements.drawerTitle.textContent = "结果面板";
            elements.drawerSubtitle.textContent = "查看结构化结果、方案切片和当前请求摘要。";
        }
    }

    function fillSecondHandCase() {
        applyMode("project");
        elements.topicInput.value = "校园二手交易平台";
        elements.directionInput.value = "Java Web";
        elements.techStackInput.value = "Spring Boot, Vue, MySQL";
        elements.primaryInput.value = "要求包含用户、商品、订单、评价和后台管理模块，并在完整方案里联动给出数据库、接口、项目结构、测试和答辩产物。";
        elements.secondaryInput.value = "请尽量贴近本科软件课设答辩场景，方案和产物全部使用中文。";
        updateSummary();
    }

    function fillScoreSystemCase() {
        applyMode("project");
        elements.topicInput.value = "学生成绩管理系统";
        elements.directionInput.value = "Java Web";
        elements.techStackInput.value = "Spring Boot, Thymeleaf, MySQL";
        elements.primaryInput.value = "需要包含学生、课程、成绩、教师和统计分析模块，并自动联动生成报告大纲和测试用例。";
        elements.secondaryInput.value = "希望整体复杂度适合本科课设，便于功能演示和报告撰写。";
        updateSummary();
    }

    function fillReviewCase() {
        applyMode("review");
        elements.topicInput.value = "学生成绩管理系统";
        elements.directionInput.value = "Java Web";
        elements.techStackInput.value = "Spring Boot, Thymeleaf, MySQL";
        elements.primaryInput.value = "当前方案包含需求分析、数据库设计和部分功能模块说明，但还没有接口设计、测试用例和答辩准备。";
        elements.secondaryInput.value = "请重点检查完整性、答辩材料准备情况以及是否缺少关键产物。";
        updateSummary();
    }

    function resetForm() {
        elements.topicInput.value = "";
        elements.directionInput.value = "";
        elements.techStackInput.value = "";
        elements.primaryInput.value = "";
        elements.secondaryInput.value = "";
        updateSummary();
    }

    function resetConversation() {
        clearMessages();
        clearResultPanels();
        resetForm();
        closeDrawer();
        elements.mainOutput.innerHTML = `
            <div class="welcome-panel">
                <h3>已开启新对话</h3>
                <p>你可以重新提问、重新生成 PAR 完整方案，或先上传课设资料增强知识库。</p>
            </div>
        `;
        setStatus("待命中", "");
    }

    function ensureField(value, fieldName) {
        const trimmed = (value || "").trim();
        if (!trimmed) {
            throw new Error(fieldName + "不能为空");
        }
        return trimmed;
    }

    function readableArtifactType(type) {
        return artifactMeta[type] || type;
    }

    function readableFileSize(size) {
        if (size == null || isNaN(size)) {
            return "未知";
        }
        const numeric = Number(size);
        if (numeric < 1024) {
            return numeric + " B";
        }
        if (numeric < 1024 * 1024) {
            return (numeric / 1024).toFixed(1) + " KB";
        }
        return (numeric / (1024 * 1024)).toFixed(1) + " MB";
    }

    function copyText(text) {
        if (!navigator.clipboard) {
            appendSystemHint("当前浏览器环境不支持复制。");
            return;
        }
        navigator.clipboard.writeText(text || "");
        setStatus("已复制", "status-success");
    }

    function scrollToBottom() {
        elements.mainOutput.scrollTop = elements.mainOutput.scrollHeight;
    }

    function parseSseEvent(segment) {
        const event = { event: "", data: "" };
        segment.split("\n").forEach((line) => {
            if (line.startsWith("event:")) {
                event.event = line.slice(6).trim();
            }
            if (line.startsWith("data:")) {
                event.data += line.slice(5).trim();
            }
        });
        return event;
    }

    function renderMarkdown(text) {
        const safe = escapeHtml(text || "");
        const codeBlocks = [];
        let transformed = safe.replace(/```([\s\S]*?)```/g, function (_, code) {
            const token = "__CODE_BLOCK_" + codeBlocks.length + "__";
            codeBlocks.push("<pre><code>" + code + "</code></pre>");
            return token;
        });

        transformed = transformed
            .replace(/^### (.*)$/gm, "<h3>$1</h3>")
            .replace(/^## (.*)$/gm, "<h2>$1</h2>")
            .replace(/^# (.*)$/gm, "<h1>$1</h1>")
            .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
            .replace(/`([^`\n]+)`/g, "<code>$1</code>");

        const lines = transformed.split("\n");
        let html = "";
        let inList = false;
        lines.forEach((line) => {
            if (/^\s*[-*] /.test(line)) {
                if (!inList) {
                    html += "<ul>";
                    inList = true;
                }
                html += "<li>" + line.replace(/^\s*[-*] /, "") + "</li>";
                return;
            }
            if (inList) {
                html += "</ul>";
                inList = false;
            }
            if (!line.trim()) {
                return;
            }
            if (/^<h[1-3]>/.test(line) || /^<pre>/.test(line)) {
                html += line;
            } else {
                html += "<p>" + line + "</p>";
            }
        });
        if (inList) {
            html += "</ul>";
        }
        codeBlocks.forEach((block, index) => {
            html = html.replace("__CODE_BLOCK_" + index + "__", block);
        });
        return html;
    }

    function escapeHtml(value) {
        return String(value == null ? "" : value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }

    function saveJson(key, value) {
        localStorage.setItem(key, JSON.stringify(value));
    }

    function loadJson(key, fallback) {
        try {
            const raw = localStorage.getItem(key);
            return raw ? JSON.parse(raw) : fallback;
        } catch (error) {
            return fallback;
        }
    }
})();
