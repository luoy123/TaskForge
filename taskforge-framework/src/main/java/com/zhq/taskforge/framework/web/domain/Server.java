package com.zhq.taskforge.framework.web.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.zhq.taskforge.common.utils.ip.IpUtils;
import com.zhq.taskforge.framework.web.domain.server.Cpu;
import com.zhq.taskforge.framework.web.domain.server.Jvm;
import com.zhq.taskforge.framework.web.domain.server.Mem;
import com.zhq.taskforge.framework.web.domain.server.Sys;
import com.zhq.taskforge.framework.web.domain.server.SysFile;

import cn.hutool.core.util.NumberUtil;
import lombok.Data;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

/**
 * 服务器信息聚合。每次请求 new 一个，调 copyTo() 填数据，不要做成单例。
 */
@Data
public class Server {

    /** CPU 两次采样的间隔（毫秒） */
    private static final int OSHI_WAIT_MILLIS = 1000;

    private Cpu cpu = new Cpu();

    private Mem mem = new Mem();

    private Jvm jvm = new Jvm();

    private Sys sys = new Sys();

    private List<SysFile> sysFiles = new ArrayList<>();

    public void copyTo() {
        //能够获取到cpu、内存、系统、jvm、文件系统等信息
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        setCpuInfo(hal.getProcessor());
        setMemInfo(hal.getMemory());
        setSysInfo();
        setJvmInfo();
        setSysFiles(si.getOperatingSystem());
    }

    private void setCpuInfo(CentralProcessor processor) {
        //1.第一次采样：获取各种状态的cpu的tickes
        long[] prevTicks =processor.getSystemCpuLoadTicks();
        //2.休眠1s：
        /** 因为通过getSystemCpuLoadTicks()获取的是从开始到现在所有的ticks,这是非常庞大的
        所以我们采用计算百分比的方式 */
        //这里调用的是oshi的工具类，实际上使用Thead.sleep也是可以的，但是这样的话我们需要自己
        //来添加try...catch方法，比较臃肿。
        Util.sleep(OSHI_WAIT_MILLIS);

        //3.第二次采样
        long[] nextTicks = processor.getSystemCpuLoadTicks();

        //4.计算各种tickType的差值
        long user = nextTicks[TickType.USER.getIndex()] - prevTicks[TickType.USER.getIndex()];
        long nice = nextTicks[TickType.NICE.getIndex()] - prevTicks[TickType.NICE.getIndex()];
        long system = nextTicks[TickType.SYSTEM.getIndex()] - prevTicks[TickType.SYSTEM.getIndex()];
        long idle = nextTicks[TickType.IDLE.getIndex()] - prevTicks[TickType.IDLE.getIndex()];
        long iowait = nextTicks[TickType.IOWAIT.getIndex()] - prevTicks[TickType.IOWAIT.getIndex()];
        long irq = nextTicks[TickType.IRQ.getIndex()] - prevTicks[TickType.IRQ.getIndex()];
        long softirq = nextTicks[TickType.SOFTIRQ.getIndex()] - prevTicks[TickType.SOFTIRQ.getIndex()];
        long steal = nextTicks[TickType.STEAL.getIndex()] - prevTicks[TickType.STEAL.getIndex()];
        //总的时间片。
        long total = user + nice + system + idle + iowait + irq + softirq + steal;


        //6.设置cpu信息
        cpu.setCpuNum(processor.getLogicalProcessorCount());
        cpu.setTotal(total);
        cpu.setSys(system);
        cpu.setWait(iowait);
        cpu.setUsed(user);
        cpu.setFree(idle);
        // ========== 【学员填写 N1-①】结束 ==========
    }

    private void setMemInfo(GlobalMemory memory) {
        // 总内存、可用内存从 memory 取；已用 = 总 - 可用。传字节，Mem 的 getter 会换算成 GB
       //1.获取物理总内存
       long totalMemory =   memory.getTotal();
       //2.获取物理可用内存
       long availableMemory = memory.getAvailable();
       mem.setTotal(totalMemory);
       mem.setUsed(totalMemory - availableMemory);
       mem.setFree(availableMemory);
    }

    private void setSysInfo() {
        Properties props = System.getProperties();
        sys.setComputerName(IpUtils.getHostName());
        sys.setComputerIp(IpUtils.getHostIp());
        sys.setOsName(props.getProperty("os.name"));
        sys.setOsArch(props.getProperty("os.arch"));
        sys.setUserDir(props.getProperty("user.dir"));
    }

    private void setJvmInfo() {
        // ========== 【学员填写 N1-③】开始 ==========
        // total / max / free 从 Runtime.getRuntime() 取（字节）
        // version / home 从 System.getProperties() 取：java.version、java.home
        long totalMemory = Runtime.getRuntime().totalMemory();
        long maxMemory = Runtime.getRuntime().maxMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        Properties props = System.getProperties();

        //2.设置属性
        jvm.setTotal(totalMemory);
        jvm.setMax(maxMemory);
        jvm.setFree(freeMemory);
        jvm.setVersion(props.getProperty("java.version"));
        jvm.setHome(props.getProperty("java.home"));
        // ========== 【学员填写 N1-③】结束 ==========
    }

    private void setSysFiles(OperatingSystem os) {
        // ========== 【学员填写 N1-④】开始 ==========
        List<OSFileStore> fileStores = os.getFileSystem().getFileStores();
        for (OSFileStore fileStore : fileStores) {
          long free = fileStore.getUsableSpace();
          long total =  fileStore.getTotalSpace();
          long used = total - free;

          //2.设置属性
          SysFile sysFile = new SysFile();
          sysFile.setFree(convertFileSize(free));
          sysFile.setTotal(convertFileSize(total));
          sysFile.setUsed(convertFileSize(used));
          sysFile.setDirName(fileStore.getMount());
          sysFile.setSysTypeName(fileStore.getType());
          sysFile.setTypeName(fileStore.getName());
          

          //防止除于0的校验。
          if(total > 0){
            sysFile.setUsage(NumberUtil.round((double)used / total * 100, 2).doubleValue());
          }else{
            sysFile.setUsage(0.0);
          }
          sysFiles.add(sysFile);

        }

        // ========== 【学员填写 N1-④】结束 ==========
    }

    /** 字节转成带单位的字符串 */
    private String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else {
            return String.format("%d B", size);
        }
    }
}
