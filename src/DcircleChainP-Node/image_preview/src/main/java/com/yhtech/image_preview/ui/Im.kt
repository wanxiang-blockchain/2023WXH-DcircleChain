package com.yhtech.image_preview.ui


class Im  {
	open class Attachment {
		var bucketId: String = ""
		var objectId: String = ""
		var type: String = ""
		var size: Long = 0L
		var key: String = ""
	}

	enum class Type(var value: Int) {
		Unknow(-1),
		Text(0),
		Image(1),
		Voice(2),
		Video(3),
		File(4);

		companion object {
			fun valueOf(value: Int): Type? {
				for (item in Type.values()) {
					if (item.value == value) {
						return item
					}
				}
				return null
			}
		}

	}

	open class ImageAttachment : Attachment() {
		var width: Int = 0
		var height: Int = 0
	}


	open class MsgImageContent {

		var type = Type.Image.value

		var thumb: ImageAttachment = ImageAttachment()
		var large: ImageAttachment = ImageAttachment()
		var original: ImageAttachment = ImageAttachment()
	}


}