/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2016, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dkf.jmule.tasks;

import android.app.Activity;
import android.content.Context;
import org.dkf.jed2k.EMuleLink;
import org.dkf.jed2k.protocol.server.SharedFileEntry;
import org.dkf.jmule.transfers.Transfer;
import org.dkf.jmule.transfers.TransferManager;
import org.dkf.jmule.util.UIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author gubatron
 * @author aldenml
 *
 */
public class StartDownloadTask extends ContextTask<Transfer> {

    private static final Logger LOG = LoggerFactory.getLogger(StartDownloadTask.class);
    private final String message;
    private SharedFileEntry entry;
    private String link = null;

    public StartDownloadTask(Context ctx, SharedFileEntry entry, String link, String message) {
        super(ctx);
        assert entry != null || link != null;
        this.entry = entry;
        this.link = link;
        this.message = message;
    }

    public StartDownloadTask(Context ctx, SharedFileEntry entry){
        this(ctx, entry, null, null);
    }

    @Override
    protected Transfer doInBackground() {
        Transfer transfer = null;
        try {
            UIUtils.showTransfersOnDownloadStart(getContext());
            if (entry != null) {
                transfer = TransferManager.instance().download(entry.hash, entry.getFileSize(), entry.getFileName());
            } else {
                EMuleLink el = EMuleLink.fromString(link);
                transfer = TransferManager.instance().download(el.hash, el.size, el.filepath);
            }
        } catch (Exception e) {
            LOG.warn("Error adding new download from result {} {}", entry, e);
            e.printStackTrace();
        }

        return transfer;
    }

    @Override
    protected void onPostExecute(Context ctx, Transfer transfer) {
        if (transfer != null) {
            if (ctx instanceof Activity) {
                //Offers.showInterstitialOfferIfNecessary((Activity) ctx);
            }

/*
            if (!(transfer instanceof InvalidTransfer)) {
                TransferManager tm = TransferManager.instance();
                if (tm.isBittorrentDownloadAndMobileDataSavingsOn(transfer)) {
                    UIUtils.showLongMessage(ctx, R.string.torrent_transfer_enqueued_on_mobile_data);
                    ((BittorrentDownload) transfer).pause();
                } else {
                    if (tm.isBittorrentDownloadAndMobileDataSavingsOff(transfer)) {
                        UIUtils.showLongMessage(ctx, R.string.torrent_transfer_consuming_mobile_data);
                    }

                    if (message != null){
                        UIUtils.showShortMessage(ctx, message);
                    }
                }
            } else {
                if (transfer instanceof ExistingDownload) {
                    //nothing happens here, the user should just see the transfer
                    //manager and we avoid adding the same transfer twice.
                } else {
                    UIUtils.showLongMessage(ctx, ((InvalidTransfer) transfer).getReasonResId());
                }
            }
            */
        }

    }
}
