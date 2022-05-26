package com.janitor.common.etcd;

import cn.hutool.json.JSONUtil;
import io.etcd.jetcd.*;
import io.etcd.jetcd.common.exception.EtcdException;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * ClassName EtcdServiceV3
 * Description
 *
 * @author 曦逆
 * Date 2022/5/16 14:28
 */
public class EtcdServiceV3 {
    private static final Logger logger = LoggerFactory.getLogger(EtcdServiceV3.class);
    private Client client;
    private Auth auth;
    private Watch watch;
    private final KV kv;
    private final Map<String, Watch.Watcher> mapWatcher;
    private final Lease lease;

    public EtcdServiceV3(String[] urls) {
        logger.info("EtcdService call! urls={}", Arrays.toString(urls));
        this.mapWatcher = new ConcurrentHashMap<>();
        ClientBuilder builder = Client.builder().endpoints(urls);
        this.client = builder.build();
        this.kv = this.client.getKVClient();
        this.watch = this.client.getWatchClient();
        this.lease = this.client.getLeaseClient();
    }

    public EtcdServiceV3(String user, String password, String[] urls) {
        logger.info("EtcdService call! urls={}", Arrays.toString(urls));
        this.mapWatcher = new ConcurrentHashMap<>();
        ClientBuilder builder = Client.builder().endpoints(urls);
        builder.authority("true");
        builder.user(Utils.strToBs(user));
        builder.password(Utils.strToBs(password));
        this.client = builder.build();
        this.kv = this.client.getKVClient();
        this.watch = this.client.getWatchClient();
        this.lease = this.client.getLeaseClient();
    }

    public boolean put(String key, String val, Long ttl) {
        try {
            LeaseGrantResponse leaseGrantResponse = this.lease.grant(ttl).get();
            PutOption putOption = PutOption.newBuilder().withLeaseId(leaseGrantResponse.getID()).build();
            this.kv.put(Utils.strToBs(key), Utils.strToBs(val), putOption).get();
            return true;
        } catch (ExecutionException | InterruptedException e) {
            logger.error("put config failed! key=" + key + ", value=" + val, e);
            return false;
        }
    }

    public boolean put(String key, String val) {
        try {
            this.kv.put(Utils.strToBs(key), Utils.strToBs(val)).get();
            return true;
        } catch (ExecutionException | InterruptedException e) {
            logger.error("put config failed! key=" + key + ", value=" + val, e);
            return false;
        }
    }

    public boolean putWithCas(String key, String beforeValue, String currentValue) {
        ByteSequence keyBs = Utils.strToBs(key);

        try {
            return this.kv.txn().If(new Cmp(keyBs, Cmp.Op.EQUAL, Objects.isNull(beforeValue) ? CmpTarget.version(0L) : CmpTarget.value(Utils.strToBs(beforeValue)))).Then(Op.put(keyBs, Utils.strToBs(currentValue), PutOption.DEFAULT)).commit().get().isSucceeded();
        } catch (Exception e) {
            logger.error("putWithCas config failed! key=" + key + ", value=" + currentValue, e);
            return false;
        }
    }

    public boolean delete(String key) {
        try {
            this.kv.delete(Utils.strToBs(key)).get();
            return true;
        } catch (ExecutionException | InterruptedException e) {
            logger.error("delete key config failed! key=" + key, e);
            return false;
        }
    }

    public boolean deletePrefix(String prefix) {
        DeleteOption option = DeleteOption.newBuilder().isPrefix(true).build();

        try {
            this.kv.delete(Utils.strToBs(prefix), option).get();
            return true;
        } catch (ExecutionException | InterruptedException e) {
            logger.error("delete prefix config failed! prefix=" + prefix, e);
            return false;
        }
    }

    public String get(String key) {
        try {
            GetResponse resp = this.kv.get(Utils.strToBs(key)).get();
            if (resp.getCount() > 0L) {
                return Utils.bsToStr(resp.getKvs().get(0).getValue());
            }
        } catch (ExecutionException | InterruptedException e) {
            logger.error("get config failed! key=" + key, e);
        }

        return "";
    }

    public Map<String, String> getAll() {
        Map<String, String> map = new HashMap<>();

        try {
            GetOption option = GetOption.newBuilder().withRange(Utils.ALL_KEY_CHAR_BS).build();
            GetResponse resp = this.kv.get(Utils.ALL_KEY_CHAR_BS, option).get();
            if (resp.getCount() > 0L) {
                resp.getKvs().forEach((kv) -> map.put(Utils.bsToStr(kv.getKey()), Utils.bsToStr(kv.getValue())));
            }

            return map;
        } catch (ExecutionException | InterruptedException e) {
            logger.error("get config allKey fail", e);
            return null;
        }
    }

    public Map<String, String> getPrefix(String prefix) {
        Map<String, String> map = new HashMap<>();

        try {
            GetOption option = GetOption.newBuilder().isPrefix(true).build();
            GetResponse resp = this.kv.get(Utils.strToBs(prefix), option).get();
            if (resp.getCount() > 0L) {
                resp.getKvs().forEach((kv) -> map.put(Utils.bsToStr(kv.getKey()), Utils.bsToStr(kv.getValue())));
            }

            return map;
        } catch (ExecutionException | InterruptedException e) {
            logger.error("get config failed! prefix=" + prefix, e);
            return null;
        }
    }

    public Map<String, String> getPrefixWithBs(ByteSequence prefix) {
        Map<String, String> map = new HashMap<>();

        try {
            GetOption option = GetOption.newBuilder().isPrefix(true).build();
            GetResponse resp = this.kv.get(prefix, option).get();
            if (resp.getCount() > 0L) {
                resp.getKvs().forEach((kv) -> map.put(Utils.bsToStr(kv.getKey()), Utils.bsToStr(kv.getValue())));
            }

            return map;
        } catch (ExecutionException | InterruptedException e) {
            logger.error("get config failed! prefix=" + prefix, e);
            return null;
        }
    }

    public void watch(String app, String key, String prefix, WatchAction<EtcdEventVo> action, WatchAction<Throwable> errAction, WatchAction<String> finishAction) {
        Watch.Listener listener = Watch.listener((response) -> response.getEvents().forEach((event) -> {
            EtcdEventVo vo = EtcdEventVo.valueOf(event);
            try {
                action.handler(vo);
            } catch (Exception e) {
                logger.error("watch action handler key=" + key + ", prefix=" + prefix + " error, msg:" + e.getMessage(), e);
            }
        }), (throwable) -> {
            logger.error("watch app=" + app + " key=" + key + ", prefix=" + prefix + " error, msg:" + throwable.getMessage(), throwable);
            errAction.handler(throwable);
        }, () -> {
            logger.info("watch app=" + app + " key={} onCompleted call...............", key);
            finishAction.handler("completed");
        });
        WatchOption option = WatchOption.newBuilder().isPrefix(true).withPrevKV(true).build();
        Watch.Watcher watcher = this.watch.watch(Utils.strToBs(key), option, listener);
        this.mapWatcher.put(app + "-" + key, watcher);
    }

    public void watch(String key, String prefix, WatchAction<EtcdEventVo> action, WatchAction<Throwable> errAction, WatchAction<String> finishAction) {
        Watch.Listener listener = Watch.listener((response) -> response.getEvents().forEach((event) -> {
            EtcdEventVo vo = EtcdEventVo.valueOf(event);
            try {
                action.handler(vo);
            } catch (Exception e) {
                logger.error("watch action handler key=" + key + ", prefix=" + prefix + " error, msg:" + e.getMessage(), e);
            }
        }), (throwable) -> {
            logger.error("watch key=" + key + ", prefix=" + prefix + " error, msg:" + throwable.getMessage(), throwable);
            errAction.handler(throwable);
        }, () -> {
            logger.info("watch key={} onCompleted call...............", key);
            finishAction.handler("completed");
        });
        WatchOption option = WatchOption.newBuilder().isPrefix(true).withPrevKV(true).build();
        Watch.Watcher watcher = this.watch.watch(Utils.strToBs(key), option, listener);
        this.mapWatcher.put(key, watcher);
    }

    public void cancelWatch(String app, String key) {
        try {
            Watch.Watcher w = this.mapWatcher.remove(app + "-" + key);
            if (w != null) {
                w.close();
            } else {
                logger.info("watch key[{}] not exists", key);
            }
        } catch (Exception e) {
            logger.error("cancel watch key=" + key + " failed!" + e.getMessage(), e);
        }

    }

    public void cancelWatch(String key) {
        try {
            Watch.Watcher w = this.mapWatcher.remove(key);
            if (w != null) {
                w.close();
            } else {
                logger.info("watch key[{}] not exists", key);
            }
        } catch (Exception e) {
            logger.error("cancel watch key=" + key + " failed!" + e.getMessage(), e);
        }

    }

    public void close() {
        this.mapWatcher.values().forEach((w) -> {
            try {
                w.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        });
        this.mapWatcher.clear();
        if (this.watch != null) {
            try {
                this.watch.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        this.watch = null;
        if (this.client != null) {
            try {
                this.client.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        this.client = null;
    }

    public Auth getAuth() {
        return this.client.getAuthClient();
    }

    public boolean healthCheck() {
        try {
            this.kv.get(Utils.strToBs("t")).get(3L, TimeUnit.SECONDS);
            return true;
        } catch (ExecutionException | EtcdException | TimeoutException | InterruptedException e) {
            logger.error("测试etcd地址连接出错", e);
        } finally {
            this.close();
        }

        return false;
    }

    public boolean batchOperate(List<EtcdOperation> opList) {
        List<Op> operateList = opList.stream().map((op) -> {
            switch (op.getOperateType()) {
                case PUT:
                    return io.etcd.jetcd.op.Op.put(Utils.strToBs(op.getKey()), Utils.strToBs(op.getValue()), PutOption.DEFAULT);
                case DELETE:
                    return io.etcd.jetcd.op.Op.delete(Utils.strToBs(op.getKey()), DeleteOption.DEFAULT);
                default:
                    return null;
            }
        }).collect(Collectors.toList());

        try {
            return this.kv.txn().Then(operateList.toArray(new Op[0])).commit().get().isSucceeded();
        } catch (Exception e) {
            logger.error("批量操作失败", e);
            return false;
        }
    }

    public static void main(String[] args) {
        EtcdServiceV3 etcdServiceV3 = new EtcdServiceV3("http://127.0.0.1:2379".split("#"));
        etcdServiceV3.watch("janitor", "janitor"
                , (e) -> System.out.println("succeed: {}" + JSONUtil.toJsonStr(e))
                , (e) -> System.out.println("error: {}" + JSONUtil.toJsonStr(e))
                , (e) -> System.out.println("complete: {}" + JSONUtil.toJsonStr(e))
        );
        System.out.println(etcdServiceV3.put("janitor", "hello world janitor"));
        etcdServiceV3.close();
    }
}
