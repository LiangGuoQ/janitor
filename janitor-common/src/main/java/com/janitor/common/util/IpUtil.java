package com.janitor.common.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * ClassName IpUtil
 * Description
 *
 * @author 曦逆
 * Date 2022/5/17 9:05
 */
public class IpUtil {
    public IpUtil() {
    }

    public static String[] getRealLocalIp() {
        List<String> ips = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();

            while (true) {
                NetworkInterface netInterface;
                do {
                    do {
                        do {
                            do {
                                do {
                                    if (!allNetInterfaces.hasMoreElements()) {
                                        return ips.toArray(new String[0]);
                                    }

                                    netInterface = allNetInterfaces.nextElement();
                                } while (netInterface.isLoopback());
                            } while (netInterface.isVirtual());
                        } while (!netInterface.isUp());
                    } while (netInterface.getDisplayName().startsWith("VirtualBox"));
                } while (netInterface.getDisplayName().startsWith("Vmware"));

                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress ip = addresses.nextElement();
                    if (ip instanceof Inet4Address) {
                        ips.add(ip.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Error when getting host ip address" + e.getMessage());
            return ips.toArray(new String[0]);
        }
    }

    public static String[] getLocalIp() {
        List<String> ips = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();

            while (true) {
                NetworkInterface netInterface;
                do {
                    do {
                        do {
                            do {
                                do {
                                    if (!allNetInterfaces.hasMoreElements()) {
                                        return ips.toArray(new String[0]);
                                    }

                                    netInterface = allNetInterfaces.nextElement();
                                } while (netInterface.isLoopback());
                            } while (netInterface.isVirtual());
                        } while (!netInterface.isUp());
                    } while (netInterface.getDisplayName().startsWith("VirtualBox"));
                } while (netInterface.getDisplayName().startsWith("Vmware"));

                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress ip = addresses.nextElement();
                    if (ip instanceof Inet4Address && ip.isSiteLocalAddress()) {
                        ips.add(ip.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Error when getting host ip address" + e.getMessage());
            return ips.toArray(new String[0]);
        }
    }
}

