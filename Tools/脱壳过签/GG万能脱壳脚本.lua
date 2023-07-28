--[[

    由OST收集而小许补充改进

]] --
local dumpDex = {}

function dumpDex:getTargetPackage()
	return gg.getTargetPackage()
end

function dumpDex:getPackageName()
	return self.targetPackage
end

function dumpDex:setPackageName(targetPackage)
	self.targetPackage = targetPackage
end

function dumpDex:getRootDir()
	return string.format('%s/脱壳', gg.EXT_STORAGE)
end

function dumpDex:getDumpDir()
	return string.format('%s/%s', self:getRootDir(), self:getPackageName())
end

function dumpDex:getRangeMap()
	local rangeMap = self.rangeMap
	if rangeMap then
		return rangeMap
	end

	rangeMap = {
		Jh = gg.REGION_JAVA_HEAP,
		Ch = gg.REGION_C_HEAP,
		Ca = gg.REGION_C_ALLOC,
		Cd = gg.REGION_C_DATA,
		Cb = gg.REGION_C_BSS,
		Ps = gg.REGION_PPSSPP,
		A = gg.REGION_ANONYMOUS,
		J = gg.REGION_JAVA,
		S = gg.REGION_STACK,
		As = gg.REGION_ASHMEM,
		V = gg.REGION_VIDEO,
		O = gg.REGION_OTHER,
		B = gg.REGION_BAD,
		Xa = gg.REGION_CODE_APP,
		Xs = gg.REGION_CODE_SYS

	}
	self.rangeMap = rangeMap
	return rangeMap
end

function dumpDex:getRangeText()
	local rangeMap = self:getRangeMap()
	local rangeValue = gg.getRanges()
	local ranges = {}

	for k, v in pairs(rangeMap) do
		if (rangeValue & v) == v then
			ranges[#ranges + 1] = k
			rangeValue = rangeValue & ~v
		end
	end

	return table.concat(ranges, ' | ')
end

function dumpDex:getRangeNames()
	local map = self:getRangeMap()
	local list = {}
	for k, v in pairs(map) do
		list[#list + 1] = k
	end
	return list
end

function dumpDex:selectRange()
	local names = self:getRangeNames()
	local selection = {}
	local rangeMsg = self:getRangeText()

	local curRangName = {}
	for name in string.gmatch(rangeMsg, '%w+') do
		curRangName[name] = true
	end

	for i, name in ipairs(names) do
		selection[i] = (curRangName[name] == true)
	end

	local inputs = gg.multiChoice(names, selection, string.format('当前的选择：%s                                                                                                                                                                                         A 内存 表示 gg.REGION_ANONYMOUS                                                                                             B 内存 表示 gg.REGION_BAD                                                                                             J 内存 表示 gg.REGION_JAVA                                                                                             O 内存 表示 gg.REGION_OTHER                                                                                             S 内存 表示 gg.REGION_STACK                                                                                             V 内存 表示 gg.REGION_VIDEO                                                                                                                                            As内存 表示 gg.REGION_ASHMEM                                                                                                  Ch内存 表示 gg.REGION_C_HEAP                                                                                              Ca内存 表示 gg.REGION_C_ALLOC                                                                                              Cd内存 表示 gg.REGION_C_DATA                                                                                              Cb内存 表示 gg.REGION_C_BSS                                                                                             Jh内存 表示 gg.REGION_JAVA_HEAP                                                                                             Ps内存 表示 gg.REGION_PPSSPP                                                                                           Xa内存 表示 gg.REGION_CODE_APP                                                                                             Xs内存 表示 gg.REGION_CODE_SYS                                                                         （推荐单选 O 来脱完整的Dex文件)',rangeMsg))
	if not inputs then
		return
	end

	local map = self:getRangeMap()
	local rangeValue = 0
	for i, v in pairs(inputs) do
		if v then
			local name = names[i]
			rangeValue = rangeValue | map[name]
		end
	end

	gg.setRanges(rangeValue)
end

function dumpDex:stringToHex(s)
	return (string.gsub(s, '.', function(s)
		return string.format('%02x', string.byte(s))
	end))
end

function dumpDex:getDexHeader()
	return 'dex\x0A035\x00'
end

function dumpDex:getResultsStep()
	return #self:getDexHeader()
end

function dumpDex:newDex(address)
	local dex = {}
	setmetatable(dex, {
		__index = self
	})

	function dex:getAddress()
		return address
	end

	function dex:getName()
		local dexName
		local index = self.index
		if type(index) ~= 'number' then
			error('需要 dex.index = number', 2)
		end
		if index == 1 then
			dexName = 'classes.dex'
			else
			dexName = string.format('classes%d.dex', index)
		end
		return dexName
	end

	function dex:getRangesInfo()
		local rangesInfo = self.rangesInfo
		if rangesInfo then
			return rangesInfo
		end

		local address = self:getAddress()
		local rangesInfo
		local RangesList = gg.getRangesList()
		for _, info in ipairs(RangesList) do
			local startAddr = info.start
			local endAddr = info['end']

			-- 找到 dex 在内存中的信息
			if startAddr <= address and address <= endAddr then

				-- 筛选出结束地址最大的唯一信息
				if not rangesInfo or rangesInfo['end'] < info['end'] then
					rangesInfo = info
				end
			end
		end

		if not rangesInfo then
			error(string.format('%s没有找到 rangesInfo', self:getName()), 2)
		end

		self.rangesInfo = rangesInfo
		return rangesInfo
	end

	function dex:getStartAddr()
		return self:getRangesInfo().start
	end

	function dex:getEndAddr()
		return self:getRangesInfo()['end']
	end

	function dex:getDexPosition()
		return self:getAddress() - self:getStartAddr()
	end

	function dex:assertProcess()
		local targetPackage = self:getTargetPackage()
		local packageName = self:getPackageName()

		if targetPackage ~= packageName then
			error(string.format('进程发生了改变 %s>%s', packageName, targetPackage), 2)
		end
	end

	function dex:getDumpMemoryPath(startAddr, endAddr, dumpDir, packageName)
		if not packageName then
			packageName = gg.getTargetPackage()
		end
		return string.format('%s/%s-%x-%x.bin', dumpDir, packageName, startAddr, endAddr)
	end

	function dex:dumpMemory(startAddr, endAddr, dumpDir)
		local res = gg.dumpMemory(startAddr, endAddr - 1, dumpDir)
		if res ~= true then
			return false, res
		end

		return self:getDumpMemoryPath(startAddr, endAddr, dumpDir, self:getPackageName())
	end

	function dex:dump()
		self:assertProcess()

		local startAddr = self:getStartAddr()
		local endAddr = self:getEndAddr()
		local dumpDir = self:getDumpDir()

		local path, err = self:dumpMemory(startAddr, endAddr, dumpDir)
		if not path then
			error(string.format('无法导出内存\n%s', err), 2)
		end

		return path
	end

	function dex:getSize()
		local dexSize = self.dexSize
		if dexSize then
			return dexSize
		end

		local offset = 32
		local value = {
			address = self:getAddress() + offset,
			flags = gg.TYPE_DWORD
		}
		local vlaues = {value}
		vlaues = gg.getValues(vlaues)
		dexSize = vlaues[1].value

		self.dexSize = dexSize
		return dexSize
	end

	function dex:getDexOutPath()
		return string.format('%s/%s', self:getDumpDir(), self:getName())
	end

	function dex:getDexOutFile(path)
		return assert(io.open(path, 'w'))
	end

	function dex:getDexInputFile(path)
		local f = assert(io.open(path, 'r'))
		f:seek('cur', self:getDexPosition())
		return f
	end

	function dex:checkSize()
		-- 正常情况应该 DEX大小是正数，并且小于内存的最大范围
		return dex:getSize() > 0 and dex:getSize() < (dex:getEndAddr() - dex:getStartAddr())
	end

	function dex:out(outPath)
		local binPath = self:dump()
		local inputf = self:getDexInputFile(binPath)
		local dexSize = self:getSize()
		local readSize = 1024 * 1024 * 1
		if dexSize < readSize then
			readSize = dexSize
		end

		if not outPath then
			outPath = self:getDexOutPath()
		end
		local outf = self:getDexOutFile(outPath)

		local readLength = 0

		-- 从内存导出的bin文件中提取出 DEX
		local function copy()
			while true do
				local tmp = inputf:read(readSize)
				outf:write(tmp)
				readLength = readLength + readSize

				if readLength >= dexSize then
					inputf:close()
					outf:close()
					break
				end
			end
		end

		local ok, err = pcall(copy)

		-- 删除导出的内存文件
		os.remove(binPath)

		if not ok then
			-- 复制 DEX 出错了，删除出错的文件
			os.remove(outPath)
			return false, err
		end

		-- 返回导出后的 DEX 文件路径
		return outPath
	end

	return dex
end

function dumpDex:results2dexs(results)
	local dexs = {}

	for i = 1, #results, self:getResultsStep() do
		local value = results[i]
		local address = value.address
		local dex = self:newDex(address)

		if dex and dex:checkSize() then
			local index = #dexs + 1
			dex.index = index
			dexs[index] = dex
		end
	end

	return dexs
end

function dumpDex:getDexs()

	-- 清空当前搜索结果
	gg.clearResults()

	gg.toast('正在搜索DEX文件...')

	-- DEX 文件头
	local dexhead = self:getDexHeader()

	-- h 表示以 HEX （十六进制）的方式搜索，后面跟随需要搜索的十六进制
	local text = 'h ' .. self:stringToHex(dexhead)

	-- 搜索以十六进制格式的 DEX 头信息
	local res = gg.searchNumber(text)
	if res ~= true then
		error(string.format('搜索DEX失败\n%s', res), 2)
	end

	-- 获取全部搜索结果
	local results = gg.getResults(gg.getResultsCount())

	-- 把搜索到的结果解析成 DEX 对象
	local dexs = self:results2dexs(results)

	-- 清空搜索
	gg.clearResults()

	return dexs
end

function dumpDex:getLogPath()
	return string.format('%s/dump.log', self:getDumpDir())
end

function dumpDex:getLogFile()
	return assert(io.open(self:getLogPath(), 'w'))
end

function dumpDex:start()

	local function dumpAll(dexs)
		local log
		local ok, err = pcall(function()
			log = self:getLogFile()
		end)

		-- if not ok then
		-- 	if gg.alert(string.format('无法创建LOG文件\n%s', err), '继续', '退出') ~= 1 then
		-- 		return
		-- 	end
		-- end

		local function outLog(msg)
			msg = tostring(msg)

			if log then
				log:write(msg .. '\n')
			end

			if msg:find('%S') then
				gg.toast(msg)
			end
		end

		local msg = string.format('导出包名：%s', self:getTargetPackage())
		outLog(msg)

		local successCount = 0
		local failCount = 0

		for i, dex in ipairs(dexs) do
			local name = dex:getName()
			local msg = string.format('正在导出：%s', name)
			outLog(msg)

			local path, err = dex:out()
			if not path then
				failCount = failCount + 1
				local msg = string.format('导出失败：%s', err)
				outLog(msg)
				else
				successCount = successCount + 1
				local msg = string.format('导出成功：%s', path)
				outLog(msg)

				local msg = string.format('文件大小：%s', dex:getSize())
				outLog(msg)
			end
			outLog('\n')
		end

		local msg = string.format('成功导出 %s 个DEX', successCount)
		if failCount > 0 then
			msg = msg .. '\n' .. string.format('导出失败 %s 个DEX', failCount)
		end
		msg = msg .. '\n' .. string.format('保存路径 %s', self:getDumpDir())

		outLog(msg)

		gg.alert(msg)

		-- 关闭LOG文件
		if log then
			log:close()
		end
	end

	-- 开始计时
	local startTime = os.clock()

	-- 缓存包名，避免进程被切换了导致包名不一致
	self:setPackageName(self:getTargetPackage())

	-- 获取 DEX 对象集
	local dexs = self:getDexs()

	-- 得到解析DEX的耗时时间
	local consuming = os.clock() - startTime

	local dexCount = #dexs
	local msg = string.format('耗时 %.2fs 在内存中搜索到%d个dex文件', consuming, dexCount)



	local operationNames = {'一键导出全部 Dex 文件'}





	local operationFuns = {}
	operationFuns[1] = function()
		return dumpAll(dexs)
	end

	for i, dex in ipairs(dexs) do
		local name = dex:getName()
		local text = name .. '\n' .. dex:getSize() .. '字节'
		local i = #operationNames + 1
		operationNames[i] = text



		operationFuns[i] = function()
			local path, err = dex:out()
			if not path then
				gg.alert(string.format('%s导出失败！\n\n%s', name, err))
				return
			end

			gg.alert(string.format('%s导出成功！\n\n%s', name, path))
		end
	end

	while true do
		if gg.isVisible() then

			local input = gg.choice(operationNames, nil, msg)
			if not input then
				local input2 = gg.alert('您确定要退出吗？', '确定', '最小化')
				if input2 == 1 then
					return
					else
					gg.setVisible(false)
				end

				else
				local func = operationFuns[input]
				self:trySelfCall(func)
			end
			else
			gg.sleep(100)
		end
	end
end

function dumpDex:try(err)
	gg.alert(string.format('try error:\n%s', err))
	return err
end

function dumpDex:trySelfCall(func, ...)
	local ok, err = pcall(func, self, ...)
	if not ok then
		self:try(err)
	end
end

function dumpDex:main()

	local function getMsg()
		local process = string.format('应用进程：%s', self:getTargetPackage())
		local range = string.format('内存：%s', self:getRangeText())
		local msg = process .. '\n' .. range
		return msg
	end

	while true do

		if gg.isVisible() then
			local input = gg.choice({'选择内存    (推荐单选 O 来脱完整的Dex文件)', '开始搜索内存 Dex 数据', '退出程序'}, nil, getMsg())
			if not input then
				gg.setVisible(false)

				elseif input == 1 then
				self:trySelfCall(self.selectRange)

				elseif input == 2 then
				self:trySelfCall(self.start)

				elseif input == 3 then
				return
			end
			else
			gg.sleep(100)
		end
	end
end

dumpDex:main()

