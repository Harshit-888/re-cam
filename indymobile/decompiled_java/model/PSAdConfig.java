package com.indymobile.app.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes.dex */
public class PSAdConfig {
    public float appVolume = 0.0f;
    public boolean appMuted = false;
    public boolean muteAllNetwork = false;
    public Set<String> muteNetworks = new HashSet(Arrays.asList("pangle", "vungle"));
    public boolean muteAppOpenAllNetwork = false;
    public Set<String> muteAppOpenNetworks = new HashSet(Arrays.asList("vungle"));

    public final String toString() {
        return "PSAdConfig{appVolume=" + this.appVolume + ", appMuted=" + this.appMuted + ", muteAllNetwork=" + this.muteAllNetwork + ", muteNetworks=" + this.muteNetworks + ", muteAppOpenAllNetwork=" + this.muteAppOpenAllNetwork + ", muteAppOpenNetworks=" + this.muteAppOpenNetworks + '}';
    }
}
