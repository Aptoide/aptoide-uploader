/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.webservices.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import pt.caixamagica.aptoide.uploader.model.Comment;
import pt.caixamagica.aptoide.uploader.model.Obb;

@Data public class GetApkInfoJson {

  public Apk apk;

  public String latest;

  public Malware malware;

  public Media media;

  public Meta meta;

  public Payment payment;

  public Signature signature;

  public String status;

  public ObbObject obb;

  List<Error> errors;

  public Apk getApk() {
    return apk;
  }

  public String getLatest() {
    return latest;
  }

  public Malware getMalware() {
    return malware;
  }

  public Media getMedia() {
    return media;
  }

  public Meta getMeta() {
    return meta;
  }

  public Payment getPayment() {
    return payment;
  }

  public Signature getSignature() {
    return signature;
  }

  public String getStatus() {
    return status;
  }

  public List<Error> getErrors() {
    return errors;
  }

  public ObbObject getObb() {
    return obb;
  }

  public static class Media {

    public List<String> sshots;
    public List<Screenshots> sshots_hd;
    public List<Videos> videos;

    public List<String> getSshots() {

      return this.sshots;
    }

    public void setSshots(List<String> sshots) {
      this.sshots = sshots;
    }

    public List<Screenshots> getSshots_hd() {
      return this.sshots_hd;
    }

    public List<Videos> getVideos() {
      return videos;
    }

    public void setVideos(List<Videos> videos) {
      this.videos = videos;
    }

    public static class Videos {

      public String thumb;
      public String type;
      public String url;

      public String getThumb() {
        return this.thumb;
      }

      public void setThumb(String thumb) {
        this.thumb = thumb;
      }

      public String getType() {
        return this.type;
      }

      public void setType(String type) {
        this.type = type;
      }

      public String getUrl() {
        return this.url;
      }

      public void setUrl(String url) {
        this.url = url;
      }
    }

    public static class Screenshots {

      public String path;
      public String orient;

      public String getOrient() {
        return orient;
      }

      public String getPath() {
        return path;
      }
    }
  }

  public static class Payment {

    public Number amount;

    @JsonProperty("currency_symbol") public String symbol;

    public String apkpath;
    public Metadata metadata;
    public List<PaymentServices> payment_services;
    public String status;

    public Number getAmount() {
      return this.amount;
    }

    public void setAmount(Number amount) {
      this.amount = amount;
    }

    public String getapkpath() {
      return this.apkpath;
    }

    public void setapkpath(String apkpath) {
      this.apkpath = apkpath;
    }

    public String getStatus() {
      return this.status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public List<PaymentServices> getPayment_services() {
      return this.payment_services;
    }

    public void setPayment_seCategrvices(List<PaymentServices> payment_services) {
      this.payment_services = payment_services;
    }

    public String getSymbol() {
      return symbol;
    }

    public void setSymbol(String symbol) {
      this.symbol = symbol;
    }

    public Metadata getMetadata() {
      return metadata;
    }

    public static class Metadata {

      public int id;

      public int getId() {
        return id;
      }
    }
  }

  public static class Meta {

    public List<Comment> comments;
    public String description;
    public Developer developer;
    public Likevotes likevotes;
    public String news;
    public String title;
    public String wurl;
    public Number min_age;
    public Flags flags;
    public int downloads;
    public Categories categories;

    public int getDownloads() {
      return this.downloads;
    }

    public void setDownloads(int downloads) {
      this.downloads = downloads;
    }

    public List<Comment> getComments() {
      return this.comments;
    }

    public void setComments(List<Comment> comments) {
      this.comments = comments;
    }

    public String getDescription() {
      return this.description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public Developer getDeveloper() {
      return this.developer;
    }

    public void setDeveloper(Developer developer) {
      this.developer = developer;
    }

    public Likevotes getLikevotes() {
      return this.likevotes;
    }

    public void setLikevotes(Likevotes likevotes) {
      this.likevotes = likevotes;
    }

    public String getNews() {
      return this.news;
    }

    public void setNews(String news) {
      this.news = news;
    }

    public String getTitle() {
      return this.title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getWUrl() {
      return wurl;
    }

    public Flags getFlags() {
      return flags;
    }

    public void setFlags(Flags flags) {
      this.flags = flags;
    }

    public static class Likevotes {

      public Number dislikes;
      public Number likes;
      public Number rating;
      public String uservote;

      public String getUservote() {
        return uservote;
      }

      public void setUservote(String uservote) {
        this.uservote = uservote;
      }

      public Number getDislikes() {
        return this.dislikes;
      }

      public void setDislikes(Number dislikes) {
        this.dislikes = dislikes;
      }

      public Number getLikes() {
        return this.likes;
      }

      public void setLikes(Number likes) {
        this.likes = likes;
      }

      public Number getRating() {
        return this.rating;
      }

      public void setRating(Number rating) {
        this.rating = rating;
      }
    }

    public static class Flags {

      public Votes votes;
      public String uservote;
      public Veredict veredict;

      public String getUservote() {
        return uservote;
      }

      public Votes getVotes() {
        return this.votes;
      }

      public void setVotes(Votes votes) {
        this.votes = votes;
      }

      public Veredict getVeredict() {
        return this.veredict;
      }

      public void setVeredict(Veredict veredict) {
        this.veredict = veredict;
      }
    }

    public static class Categories {

      public List<Category> standard;
      public List<Category> custom;

      public static class Category {

        public Number id;
        public Number parent;
        public String name;
      }
    }

    public static class Veredict {

      public String flag;
      public String review;

      public String getFlag() {
        return this.flag;
      }

      public void setFlag(String flag) {
        this.flag = flag;
      }

      public String getReview() {
        return this.review;
      }

      public void setReview(String review) {
        this.review = review;
      }
    }

    public static class Votes {

      public Number fake;
      public Number freeze;
      public Number good;
      public Number license;
      public Number virus;

      public Number getFake() {
        return this.fake;
      }

      public void setFake(Number fake) {
        this.fake = fake;
      }

      public Number getFreeze() {
        return this.freeze;
      }

      public void setFreeze(Number freeze) {
        this.freeze = freeze;
      }

      public Number getGood() {
        return this.good;
      }

      public void setGood(Number good) {
        this.good = good;
      }

      public Number getLicense() {
        return this.license;
      }

      public void setLicense(Number license) {
        this.license = license;
      }

      public Number getVirus() {
        return this.virus;
      }

      public void setVirus(Number virus) {
        this.virus = virus;
      }
    }

    public static class Developer {

      public Info info;
      public List<String> packages;

      public Info getInfo() {
        return this.info;
      }

      public void setInfo(Info info) {
        this.info = info;
      }

      public List<String> getPackages() {
        return this.packages;
      }

      public void setPackages(List<String> packages) {
        this.packages = packages;
      }

      public static class Info {

        public String email;
        public String name;
        public String privacy_policy;
        public String website;

        public String getEmail() {
          return this.email;
        }

        public void setEmail(String email) {
          this.email = email;
        }

        public String getName() {
          return this.name;
        }

        public void setName(String name) {
          this.name = name;
        }

        public String getPrivacy_policy() {
          return this.privacy_policy;
        }

        public void setPrivacy_policy(String privacy_policy) {
          this.privacy_policy = privacy_policy;
        }

        public String getWebsite() {
          return this.website;
        }

        public void setWebsite(String website) {
          this.website = website;
        }
      }
    }
  }

  public static class Malware {

    public Reason reason;
    public String status;

    public Reason getReason() {
      return this.reason;
    }

    public void setReason(Reason reason) {
      this.reason = reason;
    }

    public String getStatus() {
      return this.status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public static class Scanned {

      public List<Av_info> av_info;
      public String date;
      public String status;

      public List<Av_info> getAv_info() {
        return this.av_info;
      }

      public void setAv_info(List<Av_info> av_info) {
        this.av_info = av_info;
      }

      public String getDate() {
        return this.date;
      }

      public void setDate(String date) {
        this.date = date;
      }

      public String getStatus() {
        return this.status;
      }

      public void setStatus(String status) {
        this.status = status;
      }
    }

    public static class Reason {

      public Scanned scanned;
      public Signature_validated signature_validated;
      public Thirdparty_validated thirdparty_validated;
      public Manual_qa manual_qa;

      public Scanned getScanned() {
        return this.scanned;
      }

      public void setScanned(Scanned scanned) {
        this.scanned = scanned;
      }

      public Signature_validated getSignature_validated() {
        return this.signature_validated;
      }

      public void setSignature_validated(Signature_validated signature_validated) {
        this.signature_validated = signature_validated;
      }

      public Thirdparty_validated getThirdparty_validated() {
        return this.thirdparty_validated;
      }

      public void setThirdparty_validated(Thirdparty_validated thirdparty_validated) {
        this.thirdparty_validated = thirdparty_validated;
      }

      public Manual_qa getManual_qa() {
        return manual_qa;
      }

      public void setManual_qa(Manual_qa manual_qa) {
        this.manual_qa = manual_qa;
      }

      public static class Signature_validated {

        public String date;
        public String signature_from;
        public String status;

        public String getDate() {
          return this.date;
        }

        public void setDate(String date) {
          this.date = date;
        }

        public String getSignature_from() {
          return this.signature_from;
        }

        public void setSignature_from(String signature_from) {
          this.signature_from = signature_from;
        }

        public String getStatus() {
          return this.status;
        }

        public void setStatus(String status) {
          this.status = status;
        }
      }

      public static class Thirdparty_validated {

        public String date;
        public String store;

        public String getDate() {
          return this.date;
        }

        public void setDate(String date) {
          this.date = date;
        }

        public String getStore() {
          return this.store;
        }

        public void setStore(String store) {
          this.store = store;
        }
      }

      public static class Manual_qa {

        public String date;
        public String tester;
        public String status;

        public String getDate() {
          return this.date;
        }

        public void setDate(String date) {
          this.date = date;
        }

        public String getTester() {
          return tester;
        }

        public void setTester(String tester) {
          this.tester = tester;
        }

        public String getStatus() {
          return status;
        }

        public void setStatus(String status) {
          this.status = status;
        }
      }
    }
  }

  public static class Av_info {

    public List<Infection> infections;
    public String name;

    public List getInfections() {
      return this.infections;
    }

    public void setInfections(List<Infection> infections) {
      this.infections = infections;
    }

    public String getName() {
      return this.name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  public static class Infection {

    public String description;
    public String name;

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  public static class Signature {

    @JsonProperty("SHA1") public String SHA1;

    public String getSHA1() {
      return this.SHA1;
    }

    public void setSHA1(String sHA1) {
      this.SHA1 = sHA1;
    }
  }

  public static class Apk {

    public String icon;

    public Number id;

    public String md5sum;

    public Number minSdk;

    public String minScreen;

    public String packageName;

    public String path;

    public String altpath;

    public List<String> permissions;

    public String repo;

    public Number size;

    public Number vercode;

    public String vername;

    public String icon_hd;

    public String getIcon() {
      return this.icon;
    }

    public void setIcon(String icon) {
      this.icon = icon;
    }

    public Number getId() {
      return this.id;
    }

    public void setId(Number id) {
      this.id = id;
    }

    public String getMd5sum() {
      return this.md5sum;
    }

    public void setMd5sum(String md5sum) {
      this.md5sum = md5sum;
    }

    public String getPackage() {
      return this.packageName;
    }

    public void setPackage(String packageName) {
      this.packageName = packageName;
    }

    public String getPath() {
      return this.path;
    }

    public void setPath(String path) {
      this.path = path;
    }

    public List<String> getPermissions() {
      return this.permissions;
    }

    public void setPermissions(List<String> permissions) {
      this.permissions = permissions;
    }

    public String getRepo() {
      return this.repo;
    }

    public void setRepo(String repo) {
      this.repo = repo;
    }

    public Number getSize() {
      return this.size;
    }

    public void setSize(Number size) {
      this.size = size;
    }

    public Number getVercode() {
      return this.vercode;
    }

    public void setVercode(Number vercode) {
      this.vercode = vercode;
    }

    public String getVername() {
      return this.vername;
    }

    public void setVername(String vername) {
      this.vername = vername;
    }

    public String getIconHd() {
      return icon_hd;
    }

    public Number getMinSdk() {
      return minSdk;
    }

    public String getMinScreen() {
      return minScreen;
    }

    public String getAltPath() {
      return altpath;
    }
  }

  public static class ObbObject {

    public Obb main;
    public Obb patch;

    public Obb getMain() {
      return main;
    }

    public Obb getPatch() {
      return patch;
    }
  }
}
